package com.sellerinsight.seller.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.sellerinsight.commerce.auth.NaverCommerceAuthClient;
import com.sellerinsight.commerce.auth.dto.CommerceAccessToken;
import com.sellerinsight.commerce.auth.dto.IssueSellerTokenCommand;
import com.sellerinsight.common.security.CredentialEncryptor;
import com.sellerinsight.seller.api.dto.SellerAccessTokenResponse;
import com.sellerinsight.seller.domain.Seller;
import com.sellerinsight.seller.domain.SellerCredential;
import com.sellerinsight.seller.domain.SellerCredentialRepository;
import com.sellerinsight.seller.domain.SellerRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.OffsetDateTime;
import java.util.Optional;

@ExtendWith(MockitoExtension.class)
class SellerAuthServiceTest {

    @Mock
    private SellerRepository sellerRepository;

    @Mock
    private SellerCredentialRepository sellerCredentialRepository;

    @Mock
    private CredentialEncryptor credentialEncryptor;

    @Mock
    private NaverCommerceAuthClient naverCommerceAuthClient;

    @InjectMocks
    private SellerAuthService sellerAuthService;

    @Test
    void issueSellerToken() {
        Seller seller = Seller.create("seller-account-uid", "sellerinsight-store");
        SellerCredential sellerCredential = SellerCredential.create(
                seller,
                "naver-client-id",
                "encrypted-secret",
                "1234"
        );

        OffsetDateTime issuedAt = OffsetDateTime.parse("2026-04-16T12:00:00+09:00");
        OffsetDateTime expiresAt = issuedAt.plusSeconds(10800);

        when(sellerRepository.findById(1L)).thenReturn(Optional.of(seller));
        when(sellerCredentialRepository.findBySellerId(1L)).thenReturn(Optional.of(sellerCredential));
        when(credentialEncryptor.decrypt("encrypted-secret"))
                .thenReturn("$2a$10$abcdefghijklmnopqrstuv");
        when(naverCommerceAuthClient.issueSellerToken(any()))
                .thenReturn(new CommerceAccessToken(
                        "access-token",
                        "Bearer",
                        10800L,
                        issuedAt,
                        expiresAt
                ));

        SellerAccessTokenResponse response = sellerAuthService.issueSellerToken(1L);

        assertThat(response.sellerId()).isEqualTo(1L);
        assertThat(response.accountId()).isEqualTo("seller-account-uid");
        assertThat(response.accessToken()).isEqualTo("access-token");
        assertThat(response.tokenType()).isEqualTo("Bearer");
        assertThat(response.expiresIn()).isEqualTo(10800L);

        ArgumentCaptor<IssueSellerTokenCommand> captor =
                ArgumentCaptor.forClass(IssueSellerTokenCommand.class);

        verify(naverCommerceAuthClient).issueSellerToken(captor.capture());

        assertThat(captor.getValue().clientId()).isEqualTo("naver-client-id");
        assertThat(captor.getValue().clientSecret()).isEqualTo("$2a$10$abcdefghijklmnopqrstuv");
        assertThat(captor.getValue().accountId()).isEqualTo("seller-account-uid");
    }
}