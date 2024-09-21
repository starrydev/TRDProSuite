package com.starryassociates.trdpro.model.poc;

import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbAttribute;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbBean;
import java.util.List;
import java.util.Objects;

@DynamoDbBean
public class TransactionItem {

    private String itemId;
    private double itemAmount;
    private String description;
    private List<AccountSplit> accountSplits;

    @DynamoDbAttribute("itemId")
    public String getItemId() {
        return itemId;
    }

    public void setItemId(String itemId) {
        this.itemId = itemId;
    }

    @DynamoDbAttribute("itemAmount")
    public double getItemAmount() {
        return itemAmount;
    }

    public void setItemAmount(double itemAmount) {
        this.itemAmount = itemAmount;
    }

    @DynamoDbAttribute("description")
    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    @DynamoDbAttribute("accountSplits")
    public List<AccountSplit> getAccountSplits() {
        return accountSplits;
    }

    public void setAccountSplits(List<AccountSplit> accountSplits) {
        this.accountSplits = accountSplits;
    }

    // Validation method to ensure the percentage split adds up to 100%
    public void validatePercentageSplit() {
        double totalPercentage = accountSplits.stream()
                .mapToDouble(AccountSplit::getPercentage)
                .sum();
        if (Double.compare(totalPercentage, 100.0) != 0) {
            throw new IllegalArgumentException("Percentage split for item " + itemId + " does not add up to 100%.");
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        TransactionItem transactionItem = (TransactionItem) o;
        return Objects.equals(itemId, transactionItem.itemId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(itemId);
    }

    @Override
    public String toString() {
        return "TransactionItem{" +
                "itemId='" + itemId + '\'' +
                ", itemAmount=" + itemAmount +
                ", description='" + description + '\'' +
                ", accountSplits=" + accountSplits +
                '}';
    }
}
