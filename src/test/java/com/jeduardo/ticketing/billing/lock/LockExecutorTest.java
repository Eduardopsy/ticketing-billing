package com.jeduardo.ticketing.billing.lock;

import com.jeduardo.ticketing.billing.exception.NegocioException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import java.util.function.Supplier;
import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("LockExecutor - Testes Unitários")
class LockExecutorTest {

    @Mock private LockService lockService;
    @InjectMocks private LockExecutor lockExecutor;

    @Test
    @DisplayName("1. Executa ação quando lock é adquirido com sucesso")
    void executeWithLock_sucesso() {
        when(lockService.acquire(anyString(), anyLong())).thenReturn(true);

        String result = lockExecutor.executeWithLock("key-test", 5, () -> "ok");

        assertThat(result).isEqualTo("ok");
        verify(lockService).release("key-test");
    }

    @Test
    @DisplayName("2. Lança NegocioException quando lock não é adquirido")
    void executeWithLock_lockNaoAdquirido_lancaExcecao() {
        when(lockService.acquire(anyString(), anyLong())).thenReturn(false);

        assertThatThrownBy(() -> lockExecutor.executeWithLock("key-test", 5, () -> "ok"))
                .isInstanceOf(NegocioException.class)
                .hasMessageContaining("Geracao de cobranca em andamento.");

        verify(lockService, never()).release(any());
    }

    @Test
    @DisplayName("3. Garante release no finally mesmo quando action lança exceção")
    void executeWithLock_garanteUnlockNoFinally_mesmoComExcecao() {
        when(lockService.acquire(anyString(), anyLong())).thenReturn(true);

        Supplier<String> actionQueLancaExcecao = () -> {
            throw new RuntimeException("Falha na action");
        };

        assertThatThrownBy(() -> lockExecutor.executeWithLock("key-test", 5, actionQueLancaExcecao))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Falha na action");

        verify(lockService, times(1)).release("key-test");
    }
}