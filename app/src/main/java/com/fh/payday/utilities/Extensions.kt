package com.fh.payday.utilities

infix fun <T> Boolean.then(param: T): T? = if (this) param else null