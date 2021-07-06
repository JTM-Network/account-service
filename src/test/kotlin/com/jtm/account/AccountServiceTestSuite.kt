package com.jtm.account

import com.jtm.account.data.service.account.AuthServiceTest
import com.jtm.account.data.service.account.PasswordServiceTest
import com.jtm.account.data.service.account.RoleServiceTest
import com.jtm.account.data.service.account.VerifyServiceTest
import com.jtm.account.presenter.controller.AuthControllerTest
import com.jtm.account.presenter.controller.PasswordControllerTest
import com.jtm.account.presenter.controller.RoleControllerTest
import com.jtm.account.presenter.controller.VerifyControllerTest
import org.junit.runner.RunWith
import org.junit.runners.Suite

@RunWith(Suite::class)
@Suite.SuiteClasses(value = [
    RoleServiceTest::class,
    RoleControllerTest::class,

    AuthServiceTest::class,
    AuthControllerTest::class,

    VerifyServiceTest::class,
    VerifyControllerTest::class,

    PasswordServiceTest::class,
    PasswordControllerTest::class
])
class AccountServiceTestSuite