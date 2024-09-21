package com.starryassociates.trdpro.util;

import software.amazon.awssdk.enhanced.dynamodb.AttributeConverter;
import software.amazon.awssdk.enhanced.dynamodb.AttributeValueType;
import software.amazon.awssdk.enhanced.dynamodb.EnhancedType;
import software.amazon.awssdk.services.dynamodb.model.AttributeValue;

public class TripTypeConverter implements AttributeConverter<TripType> {

    @Override
    public AttributeValue transformFrom(TripType tripType) {
        return AttributeValue.builder().s(tripType.name()).build();
    }

    @Override
    public TripType transformTo(AttributeValue attributeValue) {
        return TripType.valueOf(attributeValue.s().toUpperCase().replace(" ", "_").replace("-", "_"));
    }

    @Override
    public EnhancedType<TripType> type() {
        return null;
    }

    @Override
    public AttributeValueType attributeValueType() {
        return AttributeValueType.S;
    }
}
