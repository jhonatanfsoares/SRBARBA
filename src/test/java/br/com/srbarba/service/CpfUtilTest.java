package br.com.srbarba.service;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class CpfUtilTest {

    @Test
    void aceitaCpfValidoComOuSemMascara() {
        assertTrue(CpfUtil.isValido("529.982.247-25"));
        assertTrue(CpfUtil.isValido("52998224725"));
    }

    @Test
    void rejeitaCpfInvalido() {
        assertFalse(CpfUtil.isValido("529.982.247-26"));
        assertFalse(CpfUtil.isValido("111.111.111-11"));
        assertFalse(CpfUtil.isValido("123"));
        assertFalse(CpfUtil.isValido(null));
    }

    @Test
    void formata() {
        assertEquals("529.982.247-25", CpfUtil.formatar("52998224725"));
    }
}
