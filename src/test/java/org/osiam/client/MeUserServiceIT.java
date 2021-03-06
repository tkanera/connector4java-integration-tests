/*
 * Copyright (C) 2013 tarent AG
 *
 * Permission is hereby granted, free of charge, to any person obtaining
 * a copy of this software and associated documentation files (the
 * "Software"), to deal in the Software without restriction, including
 * without limitation the rights to use, copy, modify, merge, publish,
 * distribute, sublicense, and/or sell copies of the Software, and to
 * permit persons to whom the Software is furnished to do so, subject to
 * the following conditions:
 *
 * The above copyright notice and this permission notice shall be
 * included in all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND,
 * EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF
 * MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT.
 * IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY
 * CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT,
 * TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE
 * SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */

package org.osiam.client;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import java.text.SimpleDateFormat;
import java.util.Date;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.osiam.client.exception.ConflictException;
import org.osiam.client.user.BasicUser;
import org.osiam.resources.scim.User;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestExecutionListeners;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.support.DependencyInjectionTestExecutionListener;

import com.github.springtestdbunit.DbUnitTestExecutionListener;
import com.github.springtestdbunit.annotation.DatabaseOperation;
import com.github.springtestdbunit.annotation.DatabaseSetup;
import com.github.springtestdbunit.annotation.DatabaseTearDown;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration("/context.xml")
@TestExecutionListeners({ DependencyInjectionTestExecutionListener.class,
        DbUnitTestExecutionListener.class })
@DatabaseTearDown(value = "/database_tear_down.xml", type = DatabaseOperation.DELETE_ALL)
@DatabaseSetup("/database_seed_me_user.xml")
public class MeUserServiceIT extends AbstractIntegrationTestBase {

    @Test
    public void get_current_user_basic_returns_correct_user() throws Exception {
        BasicUser basicUser = oConnector.getCurrentUserBasic(accessToken);

        assertEquals("cef9452e-00a9-4cec-a086-d171374ffbef", basicUser.getId());
        assertEquals("marissa", basicUser.getUserName());
        assertEquals("marissa@example.com", basicUser.getEmail());
        assertEquals("Marissa", basicUser.getFirstName());
        assertEquals("Thompson", basicUser.getLastName());
        assertEquals("", basicUser.getLocale());
        SimpleDateFormat sdfToDate = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        Date date = sdfToDate.parse("2011-10-10 00:00:00");
        assertEquals(date, basicUser.getUpdatedTime());
    }

    @Test
    public void get_current_user_returns_correct_user() throws Exception {
        User user = oConnector.getCurrentUser(accessToken);

        assertEquals("cef9452e-00a9-4cec-a086-d171374ffbef", user.getId());
        assertEquals("marissa", user.getUserName());
    }

    @Test (expected = ConflictException.class)
    public void get_current_user_while_loged_in_with_client_credential_raises_exception() throws Exception{
        OsiamConnector connectorForClient = new OsiamConnector.Builder()
                .setAuthServerEndpoint(AUTH_ENDPOINT_ADDRESS)
                .setResourceServerEndpoint(RESOURCE_ENDPOINT_ADDRESS)
                .setClientId(CLIENT_ID)
                .setClientSecret(CLIENT_SECRET)
                .build();

        connectorForClient.getCurrentUserBasic(connectorForClient.retrieveAccessToken());

        fail("Exception expected");
    }

}
