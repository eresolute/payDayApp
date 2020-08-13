package com.fh.payday

import com.fh.payday.utilities.PasswordValidator.Companion.validate
import junit.framework.TestCase.assertEquals
import org.junit.Test

class PasswordPolicyTest {

    @Test
    fun shouldReturnTrueOnValidPolicy1() {
        assertEquals(validate("Pass123!"), true)
    }

    @Test
    fun shouldReturnTrueOnValidPolicy2() {
        assertEquals(validate("Pass@123"), true)
    }

    @Test
    fun shouldReturnTrueOnValidPolicy3() {
        assertEquals(validate("P#ass123"), true)
    }

    @Test
    fun shouldReturnTrueOnValidPolicy4() {
        assertEquals(validate("541\$Password"), true)
    }

    @Test
    fun shouldReturnTrueOnValidPolicy5() {
        assertEquals(validate("%PASSW0rd"), true)
    }

    @Test
    fun shouldReturnTrueOnValidPolicy6() {
        assertEquals(validate("pa^Ss123"), true)
    }

    @Test
    fun shouldReturnTrueOnValidPolicy7() {
        assertEquals(validate("Pass123&"), true)
    }

    @Test
    fun shouldReturnTrueOnValidPolicy8() {
        assertEquals(validate("pasSW0rd*"), true)
    }

    @Test
    fun shouldReturnTrueOnValidPolicy9() {
        assertEquals(validate("(passW0rd"), true)
    }

    @Test
    fun shouldReturnTrueOnValidPolicy10() {
        assertEquals(validate("passW0rd)"), true)
    }

    @Test
    fun shouldReturnTrueOnValidPolicy11() {
        assertEquals(validate("541\\Password"), true)
    }

    @Test
    fun shouldReturnTrueOnValidPolicy12() {
        assertEquals(validate("PaSS/00167"), true)
    }

    @Test
    fun shouldReturnTrueOnValidPolicy13() {
        assertEquals(validate("passWORD?123"), true)
    }

    @Test
    fun shouldReturnTrueOnValidPolicy14() {
        assertEquals(validate("passWORD'123"), true)
    }

    @Test
    fun shouldReturnTrueOnValidPolicy15() {
        assertEquals(validate("passWORD\"123"), true)
    }

    @Test
    fun shouldReturnTrueOnValidPolicy16() {
        assertEquals(validate("Pass;123"), true)
    }

    @Test
    fun shouldReturnTrueOnValidPolicy17() {
        assertEquals(validate("Pass:123"), true)
    }

    @Test
    fun shouldReturnTrueOnValidPolicy18() {
        assertEquals(validate("123`Pass"), true)
    }

    @Test
    fun shouldReturnTrueOnValidPolicy19() {
        assertEquals(validate("123~Pass"), true)
    }

    @Test
    fun shouldReturnTrueOnValidPolicy20() {
        assertEquals(validate("paSs.123"), true)
    }

    @Test
    fun shouldReturnTrueOnValidPolicy21() {
        assertEquals(validate("paSs,123"), true)
    }

    @Test
    fun shouldReturnTrueOnValidPolicy22() {
        assertEquals(validate("PaSS-123"), true)
    }

    @Test
    fun shouldReturnTrueOnValidPolicy23() {
        assertEquals(validate("PaSS_123"), true)
    }

    @Test
    fun shouldReturnTrueOnValidPolicy24() {
        assertEquals(validate("+pasS123"), true)
    }

    @Test
    fun shouldReturnTrueOnValidPolicy25() {
        assertEquals(validate("=Pass123"), true)
    }

    @Test
    fun shouldReturnTrueOnValidPolicy26() {
        assertEquals(validate("{]passW123"), true)
    }

    @Test
    fun shouldReturnTrueOnValidPolicy27() {
        assertEquals(validate("!#<<>>pW1"), true)
    }

    @Test
    fun shouldReturnFalseOnLengthLessThan8() {
        assertEquals(validate("Pas@123"), false)
    }

    @Test
    fun shouldReturnFalseOnNoSpecialChar() {
        assertEquals(validate("PASS123word"), false)
    }

    @Test
    fun shouldReturnFalseOnNoUpperCase() {
        assertEquals(validate("password@123"), false)
    }

    @Test
    fun shouldReturnFalseOnNoLowerCase() {
        assertEquals(validate("PASSWORD@123"), false)
    }

    @Test
    fun shouldReturnFalseOnNoNumeric() {
        assertEquals(validate("Password@"), false)
    }
}