package ru.tinkoff.piapi.robot.services.mapper;

import org.mapstruct.CollectionMappingStrategy;
import org.mapstruct.Context;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.NullValueCheckStrategy;
import ru.tinkoff.piapi.contract.v1.Bond;
import ru.tinkoff.piapi.contract.v1.Currency;
import ru.tinkoff.piapi.contract.v1.Etf;
import ru.tinkoff.piapi.contract.v1.Future;
import ru.tinkoff.piapi.contract.v1.Share;
import ru.tinkoff.piapi.robot.db.entities.Instrument;

import java.util.List;

@Mapper(componentModel = "spring",
        collectionMappingStrategy = CollectionMappingStrategy.ADDER_PREFERRED,
        nullValueCheckStrategy = NullValueCheckStrategy.ALWAYS)
public interface InstrumentMapper {


    @Mapping(target = "instrumentType", expression = "java(instrumentType)")
    Instrument mapEtf(Etf etf, @Context String instrumentType);
    List<Instrument> mapEtf(List<Etf> etf, @Context String instrumentType);

    @Mapping(target = "instrumentType", expression = "java(instrumentType)")
    Instrument mapShare(Share share, @Context String instrumentType);
    List<Instrument> mapShare(List<Share> share, @Context String instrumentType);

    @Mapping(target = "instrumentType", expression = "java(instrumentType)")
    Instrument mapCurrency(Currency currency, @Context String instrumentType);
    List<Instrument> mapCurrency(List<Currency> currency, @Context String instrumentType);

    @Mapping(target = "instrumentType", expression = "java(instrumentType)")
    Instrument mapBond(Bond bond, @Context String instrumentType);
    List<Instrument> mapBond(List<Bond> bond, @Context String instrumentType);

    @Mapping(target = "instrumentType", expression = "java(instrumentType)")
    Instrument mapFuture(Future future, @Context String instrumentType);
    List<Instrument> mapFuture(List<Future> future, @Context String instrumentType);
}
