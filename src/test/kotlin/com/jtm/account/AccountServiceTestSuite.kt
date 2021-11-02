package com.jtm.account

import com.jtm.account.data.service.ApiServiceTest
import com.jtm.account.data.service.account.*
import com.jtm.account.presenter.controller.*
import org.junit.runner.RunWith
import org.junit.runners.Suite

@RunWith(Suite::class)
@Suite.SuiteClasses(value = [
    RoleServiceTest::class,
    ApiServiceTest::class,
    PasswordServiceTest::class,
    VerifyServiceTest::class,
    AuthServiceTest::class,
    AdminServiceTest::class,

    RoleControllerTest::class,
    AuthControllerTest::class,
    VerifyControllerTest::class,
    PasswordControllerTest::class,
    ApiControllerTest::class,
    AdminControllerTest::class
])
class AccountServiceTestSuite