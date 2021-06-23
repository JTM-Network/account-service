package com.jtm.account

import com.jtm.account.data.service.AccountProfileServiceTest
import com.jtm.account.data.service.RoleServiceTest
import com.jtm.account.presenter.controller.AuthControllerTest
import com.jtm.account.presenter.controller.RoleControllerTest
import org.junit.runner.RunWith
import org.junit.runners.Suite

@RunWith(Suite::class)
@Suite.SuiteClasses(value = [
    RoleServiceTest::class,
    RoleControllerTest::class,

    AccountProfileServiceTest::class,
    AuthControllerTest::class
])
class AccountServiceTestSuite