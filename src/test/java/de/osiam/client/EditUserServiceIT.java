package de.osiam.client;

import com.github.springtestdbunit.DbUnitTestExecutionListener;
import com.github.springtestdbunit.annotation.DatabaseSetup;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.osiam.client.OsiamUserService;
import org.osiam.client.exception.ConflictException;
import org.osiam.client.query.Query;
import org.osiam.client.query.QueryResult;
import org.osiam.client.query.metamodel.User_;
import org.osiam.resources.scim.Address;
import org.osiam.resources.scim.MultiValuedAttribute;
import org.osiam.resources.scim.Name;
import org.osiam.resources.scim.User;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestExecutionListeners;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.support.DependencyInjectionTestExecutionListener;

import java.util.*;

import static junit.framework.Assert.*;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration("/context.xml")
@TestExecutionListeners({DependencyInjectionTestExecutionListener.class,
        DbUnitTestExecutionListener.class})
@DatabaseSetup("/database_seed.xml")
public class EditUserServiceIT extends AbstractIntegrationTestBase{

    private OsiamUserService service;

    @Before
    public void setUp() throws Exception {
        service = new OsiamUserService.Builder(endpointAddress).build();
    }

    @Test (expected = ConflictException.class)
    public void create_user_with_no_username_raises_exception(){
        User newUser = new User.Builder().build();
        service.createUser(newUser, accessToken);
        fail("Exception excpected");
    }

    @Test (expected = ConflictException.class)
    public void create_user_with_exisitng_username_raises_exception(){
        User newUser = new User.Builder("hsimpson").build();
        service.createUser(newUser, accessToken);
        fail("Exception excpected");
    }

    @Test (expected = ConflictException.class)
    public void create_empty_user_raises_exception(){
        User newUser = new User.Builder("").build();
        service.createUser(newUser, accessToken);
        fail("Exception excpected");
    }

    @Test
    public void create_simple_User(){
        User newUser = new User.Builder("csu").build();
        User savedUser = service.createUser(newUser, accessToken);
        assertTrue(savedUser.getId().length() > 0);
        User dbUSer = service.getUserByUUID(UUID.fromString(savedUser.getId()), accessToken);
        assertEquals(newUser.getUserName(), dbUSer.getUserName());
    }

    @Test
    public void create_user_with_existing_uuid(){
        String hSimpsonId = "7d33bcbe-a54c-43d8-867e-f6146164941e";
        User newUser = new User.Builder("crweu").setId(hSimpsonId).build();
        service.createUser(newUser, accessToken);
        User dbUser = service.getUserByUUID(UUID.fromString(hSimpsonId), accessToken);

        assertEquals("hsimpson", dbUser.getUserName());
    }

    @Test
    public void given_uuid_to_new_user_has_changed_after_saving()
    {
        String userId = "1d33bcbe-a54c-43d8-867e-f6146164941e";
        User newUser = new User.Builder("gutnuhcas").setId(userId).build();
        User savedUser = service.createUser(newUser, accessToken);

        assertNotSame(userId, savedUser.getId());
    }

    @Test
    public void created_user_can_be_found(){
        String userName = "cucbf";

        Query query = new Query.Builder(User.class).filter(new Query.Filter(User.class).startsWith(User_.userName.equalTo(userName))).build();
        QueryResult<User> result = service.searchUsers(query, accessToken);
        assertEquals(0, result.getResources().size());

        User newUser = new User.Builder(userName).build();
        service.createUser(newUser, accessToken);

        result = service.searchUsers(query, accessToken);
        assertEquals(1, result.getResources().size());
        User dbUser = result.getResources().get(0);
        assertNotSame(userName, dbUser.getUserName());
    }

    @Test
    public void uuid_return_user_same_as_new_loaded_uuid(){
        String userId = "1d33bcbe-a54c-43d8-867e-f6146164941e";
        String userName = "gutnuhcas";
        User newUser = new User.Builder(userName).setId(userId).build();
        User savedUser = service.createUser(newUser, accessToken);
        Query query = new Query.Builder(User.class).filter(new Query.Filter(User.class).startsWith(User_.userName.equalTo(userName))).build();
        QueryResult<User> result = service.searchUsers(query, accessToken);

        assertEquals(1, result.getResources().size());
        User dbUser = result.getResources().get(0);
        assertEquals(savedUser.getId(), dbUser.getId());
    }

    @Test
    @Ignore
    public void create_complete_user(){

        User newUser = createCompleteUser();
        User savedUser = service.createUser(newUser, accessToken);
        Query query = new Query.Builder(User.class).filter(new Query.Filter(User.class).startsWith(User_.userName.equalTo(newUser.getUserName()))).build();
        QueryResult<User> result = service.searchUsers(query, accessToken);

        assertEquals(1, result.getResources().size());
        User dbUser = result.getResources().get(0);
        assertEquals(savedUser.getId(), dbUser.getId());
    }

    private User createCompleteUser(){
        User user = null;

        boolean active = true;
        Set<Object> any = new HashSet<Object>(Arrays.asList("anyStatement"));
        Address address = new Address.Builder()
                .setStreetAddress("Example Street 22")
                .setCountry("Germany")
                .setFormatted("Complete Adress")
                .setLocality("de")
                .setPostalCode("111111")
                .setRegion("Berlin")
                .build();
        List<Address> addresses = new ArrayList<>();
        addresses.add(address);
        MultiValuedAttribute email01 = new MultiValuedAttribute.Builder().setValue("example@example.de").setPrimary(true).setType("email").build();
        MultiValuedAttribute email02 = new MultiValuedAttribute.Builder().setValue("example02@example.de").setPrimary(false).setType("email").build();
        List<MultiValuedAttribute> emails = new ArrayList<>();
        emails.add(email01);
        emails.add(email02);
        Name name = new Name.Builder().setFamilyName("familyName")
                .setGivenName("vorName")
                .setMiddleName("middle")
                .setFormatted("complete Name")
                .setHonorificPrefix("HPre")
                .setHonorificSuffix("HSu").build();

        user = new User.Builder("completeU")
                .setPassword("password")
                .setActive(true)
                .setAny(any)
                .setAddresses(addresses)
                //.setDisplayName("myDisplayName")
                //.setEmails(emails)
                //.setGroups()
                  //.setIms()
                //.setLocale("de")
               // .setName(name)
                //.setEntitlements()
                //.setNickName("aNicknane")
                //.setPhoneNumbers()
                //.setPhotos()
                //.setPreferredLanguage("german")
                //.setProfileUrl("")
                //.setRoles()
                //.setTimezone("UTF")
                //.setTitle("Dr.")

                .build();

        return user;
    }

}
