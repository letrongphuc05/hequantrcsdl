package CarRental.example.service.sepay;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class SepayWebhookData {
    @JsonProperty("id")
    private String tranId;

    @JsonProperty("account_number")
    private String account_number;

    @JsonProperty("bank_name")
    private String bank_name;

    private String amount;

    @JsonProperty("sub_amount")
    private String sub_amount;

    private String content;
    private String description;

    @JsonProperty("reference_number")
    private String reference_number;
}