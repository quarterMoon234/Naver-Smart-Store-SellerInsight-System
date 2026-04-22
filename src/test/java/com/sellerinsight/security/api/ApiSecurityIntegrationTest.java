package com.sellerinsight.security.api;

import com.sellerinsight.seller.domain.Seller;
import com.sellerinsight.seller.domain.SellerRepository;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.httpBasic;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class ApiSecurityIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private SellerRepository sellerRepository;

    @Test
    void healthApiIsPublic() throws Exception {
        mockMvc.perform(get("/api/v1/health"))
                .andExpect(status().isOk());
    }

    @Test
    void sellerApiRequiresAuthentication() throws Exception {
        mockMvc.perform(get("/api/v1/sellers/9999"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void sellerUserCanAccessSellerApi() throws Exception {
        mockMvc.perform(
                        get("/api/v1/sellers/9999")
                                .with(httpBasic("seller-demo", "seller-demo-1234"))
                )
                .andExpect(status().isNotFound());
    }

    @Test
    void sellerUserCanAccessCurrentSellerApi() throws Exception {
        sellerRepository.findByExternalSellerId("seller-demo")
                .orElseGet(() -> sellerRepository.saveAndFlush(
                        Seller.create("seller-demo", "seller-demo-store")
                ));

        mockMvc.perform(
                        get("/api/v1/sellers/me")
                                .with(httpBasic("seller-demo", "seller-demo-1234"))
                )
                .andExpect(status().isOk());
    }

    @Test
    void adminUserCannotAccessCurrentSellerApi() throws Exception {
        mockMvc.perform(
                        get("/api/v1/sellers/me")
                                .with(httpBasic("admin", "admin-1234"))
                )
                .andExpect(status().isForbidden());
    }

    @Test
    void sellerUserCannotAccessAdminApi() throws Exception {
        mockMvc.perform(
                        get("/api/v1/admin/pipelines/daily/runs")
                                .param("limit", "1")
                                .with(httpBasic("seller-demo", "seller-demo-1234"))
                )
                .andExpect(status().isForbidden());
    }

    @Test
    void adminUserCanAccessAdminApi() throws Exception {
        mockMvc.perform(
                        get("/api/v1/admin/pipelines/daily/runs")
                                .param("limit", "1")
                                .with(httpBasic("admin", "admin-1234"))
                )
                .andExpect(status().isOk());
    }
}
