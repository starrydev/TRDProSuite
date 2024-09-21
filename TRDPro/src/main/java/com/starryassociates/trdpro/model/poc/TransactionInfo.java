package com.starryassociates.trdpro.model.poc;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.starryassociates.trdpro.util.CryptoUtil;
import com.starryassociates.trdpro.util.TripType;
import com.starryassociates.trdpro.util.TripTypeConverter;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.*;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.*;

@JsonIgnoreProperties(ignoreUnknown = true)
@DynamoDbBean  // Specifies that this class can be mapped to a DynamoDB table
public class TransactionInfo {

    private String transactionId;
    private String merchant;
    private double totalAmount;
    private String date;
    private TripType tripType;
    private String travellerId;
    private String creditCard;
    private LocalDateTime insertDateTime;
    private List<TransactionItem> transactionItems;

    @DynamoDbPartitionKey  // Marks this field as the primary key
    @DynamoDbAttribute("transactionId")
    public String getTransactionId() {
        return transactionId;
    }

    public void setTransactionId(String transactionId) {
        this.transactionId = transactionId;
    }

    @DynamoDbAttribute("merchant")  // Maps this field to a DynamoDB attribute
    public String getMerchant() {
        return merchant;
    }

    public void setMerchant(String merchant) {
        this.merchant = merchant;
    }

    @DynamoDbAttribute("totalAmount")  // Maps this field to a DynamoDB attribute
    public double getTotalAmount() {
        return totalAmount;
    }

    public void setTotalAmount(double totalAmount) {
        this.totalAmount = totalAmount;
    }

    @DynamoDbAttribute("date")  // Maps this field to a DynamoDB attribute
    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    @DynamoDbAttribute("travellerId")
    public String getTravellerId() {
        return travellerId;
    }

    public void setTravellerId(String travellerId) {
        this.travellerId = travellerId;
    }

    @DynamoDbAttribute("creditCard")
    public String getCreditCard() {
        return creditCard;
    }

    public String getCreditCardDecrpted() {
        return CryptoUtil.decrypt(creditCard);
    }

    public void setCreditCard(String creditCard) {
        this.creditCard = CryptoUtil.encrypt(creditCard);
    }

    @DynamoDbAttribute("insertDateTime")
    public LocalDateTime getInsertDateTime() {
        return insertDateTime;
    }

    public void setInsertDateTime(LocalDateTime insertDateTime) {
        this.insertDateTime = insertDateTime;
    }

    @DynamoDbAttribute("tripType")  // Maps this field to a DynamoDB attribute
    @DynamoDbConvertedBy(TripTypeConverter.class)  // Custom converter for TripType enum
    public TripType getTripType() {
        return tripType;
    }

    public void setTripType(String tripTypeString) {
        if (tripTypeString == null || tripTypeString.trim().isEmpty()) {
            throw new IllegalArgumentException("TripType string cannot be null or empty.");
        }

        // Normalize the string: trim, uppercase, and replace spaces/hyphens with underscores
        String normalizedTripTypeString = tripTypeString.trim().toUpperCase().replace(" ", "_").replace("-", "_");

        try {
            // Convert string to TripType enum
            TripType tripType = TripType.valueOf(normalizedTripTypeString);

            // Check if the trip type is enabled
            if (!tripType.isEnabled()) {
                throw new IllegalArgumentException("Invalid TripType: The selected TripType is not enabled.");
            }

            // Set the trip type using the existing setter
            setTripType(tripType);

        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Invalid TripType: No matching enabled TripType found for the provided string.", e);
        }
    }
    public void setTripType(TripType tripType) {
        if (tripType == null || !tripType.isEnabled()) {
            throw new IllegalArgumentException("Invalid TripType: The selected TripType is not enabled.");
        }
        this.tripType = tripType;
    }

    @DynamoDbAttribute("transactionItems")  // Maps the list of transaction items to a DynamoDB attribute
    public List<TransactionItem> getTransactionItems() {
        return transactionItems;
    }

    public void setTransactionItems(List<TransactionItem> transactionItems) {
        this.transactionItems = transactionItems != null ? transactionItems : new ArrayList<>();  // Ensure it's never null
    }

    // Validation method to ensure the totalAmount matches the sum of item amounts
    public void validateTransactionTotal() {
        BigDecimal calculatedTotal = transactionItems.stream()
                .map(TransactionItem::getItemAmount)
                .map(BigDecimal::valueOf)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal totalAmountBD = BigDecimal.valueOf(totalAmount);

        if (calculatedTotal.setScale(2, RoundingMode.HALF_UP).compareTo(totalAmountBD.setScale(2, RoundingMode.HALF_UP)) != 0) {
            throw new IllegalArgumentException("TransactionInfo total does not match the sum of item amounts.");
        }

        for (TransactionItem transactionItem : transactionItems) {
            transactionItem.validatePercentageSplit();
        }
    }

    public Map<String, Double> getTransactionTotalForAcctCode() {
        Map<String, Double> totalsByAccountCode = new HashMap<>();

        for (TransactionItem transactionItem : transactionItems) {
            double itemAmount = transactionItem.getItemAmount();
            for (AccountSplit split : transactionItem.getAccountSplits()) {
                String accountCode = split.getAccountCode();
                double percentage = split.getPercentage();

                double splitAmount = itemAmount * (percentage / 100.0);

                totalsByAccountCode.merge(accountCode, splitAmount, Double::sum);
            }
        }

        return totalsByAccountCode;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        TransactionInfo that = (TransactionInfo) o;
        return Objects.equals(transactionId, that.transactionId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(transactionId);
    }

    @Override
    public String toString() {
        return "TransactionInfo{" +
                "transactionId='" + transactionId + '\'' +
                ", merchant='" + merchant + '\'' +
                ", totalAmount=" + totalAmount +
                ", date='" + date + '\'' +
                ", transactionItems=" + transactionItems +
                '}';
    }
}
