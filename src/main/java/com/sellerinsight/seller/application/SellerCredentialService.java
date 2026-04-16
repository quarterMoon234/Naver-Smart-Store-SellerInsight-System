package com.sellerinsight.seller.application;

import com.sellerinsight.common.error.BusinessException;
import com.sellerinsight.common.error.ErrorCode;
import com.sellerinsight.common.security.CredentialEncryptor;
import com.sellerinsight.seller.api.dto.SellerCredentialResponse;
import com.sellerinsight.seller.api.dto.UpsertSellerCredentialRequest;
import com.sellerinsight.seller.domain.Seller;
import com.sellerinsight.seller.domain.SellerCredential;
import com.sellerinsight.seller.domain.SellerCredentialRepository;
import com.sellerinsight.seller.domain.SellerRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class SellerCredentialService {

    private final SellerRepository sellerRepository;
    private final SellerCredentialRepository sellerCredentialRepository;
    private final CredentialEncryptor credentialEncryptor;

    @Transactional
    public SellerCredentialResponse upsert(
            Long sellerId,
            UpsertSellerCredentialRequest request
    ) {
        Seller seller = getSeller(sellerId);
        String encryptedClientSecret = credentialEncryptor.encrypt(request.clientSecret());
        String clientSecretHint = extractClientSecretHint(request.clientSecret());

        SellerCredential sellerCredential = sellerCredentialRepository.findBySellerId(sellerId)
                .map(existingCredential -> {
                    existingCredential.update(
                            request.clientId(),
                            encryptedClientSecret,
                            clientSecretHint
                    );
                    return sellerCredentialRepository.saveAndFlush(existingCredential);
                })
                .orElseGet(() -> sellerCredentialRepository.saveAndFlush(
                        SellerCredential.create(
                                seller,
                                request.clientId(),
                                encryptedClientSecret,
                                clientSecretHint
                        )
                ));

        return SellerCredentialResponse.from(sellerCredential);
    }

    public SellerCredentialResponse get(Long sellerId) {
        getSeller(sellerId);

        SellerCredential sellerCredential = sellerCredentialRepository.findBySellerId(sellerId)
                .orElseThrow(() -> new BusinessException(ErrorCode.SELLER_CREDENTIAL_NOT_FOUND));

        return SellerCredentialResponse.from(sellerCredential);
    }

    private Seller getSeller(Long sellerId) {
        return sellerRepository.findById(sellerId)
                .orElseThrow(() -> new BusinessException(ErrorCode.RESOURCE_NOT_FOUND));
    }

    private String extractClientSecretHint(String clientSecret) {
        int startIndex = Math.max(clientSecret.length() - 4, 0);
        return clientSecret.substring(startIndex);
    }
}
