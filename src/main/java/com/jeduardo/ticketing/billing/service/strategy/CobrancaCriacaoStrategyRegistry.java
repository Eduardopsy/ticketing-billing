package com.jeduardo.ticketing.billing.service.strategy;

import com.jeduardo.ticketing.billing.domain.enums.CobrancaMetodoEnum;
import com.jeduardo.ticketing.billing.exception.NegocioException;
import org.springframework.stereotype.Component;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

@Component
public class CobrancaCriacaoStrategyRegistry {

    private final Map<CobrancaMetodoEnum, CobrancaCriacaoStrategy> strategies =
            new EnumMap<>(CobrancaMetodoEnum.class);

    public CobrancaCriacaoStrategyRegistry(List<CobrancaCriacaoStrategy> strategyList) {
        strategyList.forEach(s -> strategies.put(s.getMetodo(), s));
    }

    public CobrancaCriacaoStrategy getStrategy(CobrancaMetodoEnum metodo) {
        CobrancaCriacaoStrategy strategy = strategies.get(metodo);
        if (strategy == null) {
            throw new NegocioException("Estratégia não encontrada para o método: " + metodo);
        }
        return strategy;
    }
}