package com.example.wallet.api;

import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import com.example.wallet.service.BulkTransferResult;
import com.example.wallet.service.BulkTransferService;

class BulkTransferControllerTest {

    private BulkTransferService service;
    private MockMvc mvc;

    @BeforeEach
    void setUp() {
        service = mock(BulkTransferService.class);
        mvc = MockMvcBuilders.standaloneSetup(new BulkTransferController(service))
                .setControllerAdvice(new ApiExceptionHandler())
                .build();
    }

    @Test
    void returnsTheBatchOutcome() throws Exception {
        when(service.transferAll(anyList())).thenReturn(
                new BulkTransferResult(1, List.of(new BulkTransferResult.Failure(1, "Insufficient funds"))));

        mvc.perform(post("/transfers/bulk")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {"transfers":[
                          {"fromAccountId":1,"toAccountId":2,"amount":5.00},
                          {"fromAccountId":1,"toAccountId":3,"amount":900.00}
                        ]}"""))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.succeeded").value(1))
                .andExpect(jsonPath("$.failures[0].index").value(1));
    }

    @Test
    void rejectsAnEmptyBatch() throws Exception {
        mvc.perform(post("/transfers/bulk")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"transfers\":[]}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void rejectsAnInvalidItem() throws Exception {
        mvc.perform(post("/transfers/bulk")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"transfers\":[{\"fromAccountId\":1,\"toAccountId\":2,\"amount\":-3}]}"))
                .andExpect(status().isBadRequest());
    }
}
