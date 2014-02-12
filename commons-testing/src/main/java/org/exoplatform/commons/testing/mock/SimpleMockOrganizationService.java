/**
 * 
 */
package org.exoplatform.commons.testing.mock;

import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import org.exoplatform.commons.utils.ListAccess;
import org.exoplatform.commons.utils.ObjectPageList;
import org.exoplatform.commons.utils.PageList;
import org.exoplatform.container.component.ComponentPlugin;
import org.exoplatform.services.organization.*;

/**
 * A partial implementation of OrganizationService for use in tests of classes
 * that rely on it. It provides a simple method to insert the membership data :
 * {@link #addMemberships(String, String...)} Uses dumb implementations of
 * Group, User and Membership based on an identifier. All find operations are
 * implemented. Write operations are not implemented.
 * 
 * @author patricelamarque
 */
@SuppressWarnings("deprecation")
public class SimpleMockOrganizationService implements OrganizationService {

  private Set<SimpleMembership> storage = new HashSet<SimpleMembership>();

  /**
   * Insert a new membership. The user, group and membership type are added to
   * the model if they don't exist.
   * 
   * @param user username username for the membership
   * @param membershipExprs membership expressions in the classical form
   *          [TYPE:]GROUP . TYPE may be omitted. In that case "*" membership
   *          will be used.
   */
  public void addMemberships(String user, String... membershipExprs) {
    for (String membershipExpr : membershipExprs) {

      String membershipType = "*";
      String group = membershipExpr;

      if (membershipExpr.indexOf(':') > 0) {
        String[] parts = membershipExpr.split(":");
        membershipType = parts[0];
        group = parts[1];
      }

      storage.add(new SimpleMembership(user, group, membershipType));

    }
  }

  public GroupHandler getGroupHandler() {
    return new MockGroupHandler();
  }

  public MembershipHandler getMembershipHandler() {
    return new MockMembershipHandler();
  }

  public MembershipTypeHandler getMembershipTypeHandler() {
    return null;
  }

  public UserHandler getUserHandler() {
    return new MockUserHandler();
  }

  public UserProfileHandler getUserProfileHandler() {
    return null;
  }

  public void addListenerPlugin(ComponentPlugin listener) throws Exception {

  }

  class MockGroupHandler implements GroupHandler {

    public void saveGroup(Group group, boolean broadcast) throws Exception {
    }

    public Group removeGroup(Group group, boolean broadcast) throws Exception {
      return null;
    }

    public Collection<Group> getAllGroups() throws Exception {
      Collection<Group> groups = new HashSet<Group>();
      Iterator<SimpleMembership> mbIt = storage.iterator();
      while (mbIt.hasNext()) {
        SimpleMembership membership = (SimpleMembership) mbIt.next();
        groups.add(new SimpleGroup(membership.getGroupId()));
      }
      return groups;
    }

    public Collection<Group> findGroupsOfUser(String user) throws Exception {
      Collection<Group> groups = new HashSet<Group>();
      Iterator<SimpleMembership> mbIt = storage.iterator();
      while (mbIt.hasNext()) {
        SimpleMembership membership = (SimpleMembership) mbIt.next();
        if (membership.getUserName().equals(user)) {
          groups.add(new SimpleGroup(membership.getGroupId()));
        }
      }
      return groups;
    }

    public Collection<Group> findGroups(Group parent) throws Exception {
      Collection<Group> groups = new HashSet<Group>();
      Iterator<SimpleMembership> mbIt = storage.iterator();
      while (mbIt.hasNext()) {
        SimpleMembership membership = (SimpleMembership) mbIt.next();
        Group group = new SimpleGroup(membership.getGroupId());
        if (group.getParentId().equals(parent.getId())) {
          groups.add(group);
        }
      }
      return groups;
    }

    public Collection<Group> findGroupByMembership(String userName, String membershipType) throws Exception {
      Collection<Group> groups = new HashSet<Group>();
      Iterator<SimpleMembership> mbIt = storage.iterator();
      while (mbIt.hasNext()) {
        SimpleMembership membership = (SimpleMembership) mbIt.next();
        if (membership.getUserName().equals(userName)
            && membership.getMembershipType().equals(membershipType)) {
          groups.add(new SimpleGroup(membership.getGroupId()));
        }
      }
      return groups;
    }

    public Group findGroupById(String groupId) throws Exception {
      Iterator<SimpleMembership> mbIt = storage.iterator();
      while (mbIt.hasNext()) {
        SimpleMembership membership = (SimpleMembership) mbIt.next();
        Group group = new SimpleGroup(membership.getGroupId());
        if (group.getId().equals(groupId)) {
          return group;
        }
      }
      return null;
    }

    public Group createGroupInstance() {
      return null;
    }

    public void createGroup(Group group, boolean broadcast) throws Exception {

    }

    public void addGroupEventListener(GroupEventListener listener) {

    }

    public void addChild(Group parent, Group child, boolean broadcast) throws Exception {

    }

    @Override
    public void removeGroupEventListener(GroupEventListener listener) {
      
    }
    @Override
    public Collection<Group> resolveGroupByMembership(String userName, String membershipType) throws Exception {
        return null;
    }


  }

  class MockMembershipHandler implements MembershipHandler {

    public MockMembershipHandler() {
    }

    public Collection<Membership> removeMembershipByUser(String username, boolean broadcast) throws Exception {
      return null;
    }

    public Membership removeMembership(String id, boolean broadcast) throws Exception {
      return null;
    }

    public void linkMembership(User user, Group group, MembershipType m, boolean broadcast) throws Exception {
      addMemberships(user.getUserName(), m.getName() + ":" + group.getId());

    }

    public Collection<Membership> findMembershipsByUserAndGroup(String userName, String groupId) throws Exception {
      Collection<Membership> memberships = new HashSet<Membership>();
      Iterator<SimpleMembership> mbIt = storage.iterator();
      while (mbIt.hasNext()) {
        SimpleMembership membership = (SimpleMembership) mbIt.next();
        if (membership.getUserName().equals(userName) && membership.getGroupId().equals(groupId)) {
          memberships.add(membership);
        }
      }
      return memberships;
    }

    public Collection<Membership> findMembershipsByUser(String userName) throws Exception {
      Collection<Membership> memberships = new HashSet<Membership>();
      Iterator<SimpleMembership> mbIt = storage.iterator();
      while (mbIt.hasNext()) {
        SimpleMembership membership = (SimpleMembership) mbIt.next();
        if (membership.getUserName().equals(userName)) {
          memberships.add(membership);
        }
      }
      return memberships;
    }

    public Collection<Membership> findMembershipsByGroup(Group group) throws Exception {
      Collection<Membership> memberships = new HashSet<Membership>();
      Iterator<SimpleMembership> mbIt = storage.iterator();
      while (mbIt.hasNext()) {
        SimpleMembership membership = (SimpleMembership) mbIt.next();
        if (membership.getGroupId().equals(group.getId())) {
          memberships.add(membership);
        }
      }
      return memberships;
    }

    public Membership findMembershipByUserGroupAndType(String userName, String groupId, String type) throws Exception {
      Iterator<SimpleMembership> mbIt = storage.iterator();
      while (mbIt.hasNext()) {
        SimpleMembership membership = (SimpleMembership) mbIt.next();
        if (membership.getUserName().equals(userName) && membership.getGroupId().equals(groupId)
            && membership.getMembershipType().equals(type)) {
          return membership;
        }
      }
      return null;
    }

    public Membership findMembership(String id) throws Exception {
      Iterator<SimpleMembership> mbIt = storage.iterator();
      while (mbIt.hasNext()) {
        SimpleMembership membership = (SimpleMembership) mbIt.next();
        if (membership.getId().equals(id)) {
          return membership;
        }
      }
      return null;
    }

    public Membership createMembershipInstance() {
      return null;
    }

    public void createMembership(Membership m, boolean broadcast) throws Exception {
    }

    public void addMembershipEventListener(MembershipEventListener listener) {
    }

    @Override
    public ListAccess<Membership> findAllMembershipsByGroup(Group group) throws Exception {
      return null;
    }

    @Override
    public void removeMembershipEventListener(MembershipEventListener listener) {
      
    }
  }

  class MockUserHandler implements UserHandler {

    public void saveUser(User user, boolean broadcast) throws Exception {
    }

    public User removeUser(String userName, boolean broadcast) throws Exception {
      return null;
    }

    @SuppressWarnings("unchecked")
    public PageList<User> getUserPageList(int pageSize) throws Exception {
      Iterator<SimpleMembership> mbIt = storage.iterator();
      HashSet<User> userSet = new HashSet<User>();
      while (mbIt.hasNext()) {
        SimpleMembership membership = (SimpleMembership) mbIt.next();
        userSet.add(new SimpleUser(membership.getUserName()));
      }
      return new ObjectPageList(Arrays.asList(userSet.toArray()), pageSize);
    }

    @SuppressWarnings("unchecked")
    public PageList<User> findUsersByGroup(String groupId) throws Exception {
      Iterator<SimpleMembership> mbIt = storage.iterator();
      HashSet<User> userSet = new HashSet<User>();
      while (mbIt.hasNext()) {
        SimpleMembership membership = (SimpleMembership) mbIt.next();
        if (membership.getGroupId().equals(groupId)) {
          userSet.add(new SimpleUser(membership.getUserName()));
        }
      }
      return new ObjectPageList(Arrays.asList(userSet.toArray()), 10);
    }
    @SuppressWarnings("unchecked")
    public ListAccess<User> findUsersByGroupId(String groupId, UserStatus status) throws Exception {
        return null;
    }

    @SuppressWarnings("unchecked")
    public PageList<User> findUsers(Query query) throws Exception {
      Iterator<SimpleMembership> mbIt = storage.iterator();
      HashSet<User> userSet = new HashSet<User>();
      while (mbIt.hasNext()) {
        SimpleMembership membership = (SimpleMembership) mbIt.next();
        String userName = membership.getUserName();
        if (query.getUserName().equals(userName)) {
          userSet.add(new SimpleUser(membership.getUserName()));
        }
      }
      return new ObjectPageList(Arrays.asList(userSet.toArray()), 10);
    }

    public User findUserByName(String userName) throws Exception {
      Iterator<SimpleMembership> mbIt = storage.iterator();
      while (mbIt.hasNext()) {
        SimpleMembership membership = (SimpleMembership) mbIt.next();
        if (membership.getUserName().equals(userName)) {
          return new SimpleUser(userName);
        }
      }
      return null;
    }
    public User findUserByName(String userName, UserStatus status) throws Exception {
        return null;
    }
    public User createUserInstance(String username) {
      return null;
    }

    public User createUserInstance() {
      return null;
    }

    public void createUser(User user, boolean broadcast) throws Exception {

    }

    public boolean authenticate(String username, String password) throws Exception {
      return false;
    }

    public void addUserEventListener(UserEventListener listener) {

    }

    public ListAccess<User> findAllUsers() throws Exception {
      return null;
    }
    public ListAccess<User> findAllUsers(UserStatus status) throws Exception {
        return null;
    }

    public ListAccess<User> findUsersByGroupId(String groupId) throws Exception {
      return null;
    }

    public ListAccess<User> findUsersByQuery(Query query) throws Exception {
      return null;
    }
    @Override
    public ListAccess<User> findUsersByQuery(Query query, UserStatus status) throws Exception {
        return null;
    }
    @Override
    public void removeUserEventListener(UserEventListener listener) {
      
    }
    public User setEnabled(String userName, boolean enabled, boolean broadcast) throws Exception, UnsupportedOperationException {
        return null;
    }

  }

  static class SimpleUser implements User {
    String name = null;
    String displayName = null;

    public SimpleUser(String name) {
      this.name = name;
    }

    public boolean equals(Object obj) {
      if (obj == null)
        return false;
      if (!(obj instanceof User))
        return false;
      User other = (User) obj;
      return (name.equals(other.getUserName()));
    }

    public int hashCode() {
      return super.hashCode();
    }

    public String toString() {
      return name;
    }

    public Date getCreatedDate() {
      return new Date();
    }

    public String getEmail() {
      return name;
    }

    public String getFirstName() {
      return name;
    }

    public String getFullName() {
      return name;
    }

    public Date getLastLoginTime() {
      return new Date();
    }

    public String getLastName() {
      return name;
    }

    public String getOrganizationId() {
      return name;
    }

    public String getPassword() {
      return name;
    }

    public String getUserName() {
      return name;
    }

    public void setCreatedDate(Date t) {

    }

    public void setEmail(String s) {

    }

    public void setFirstName(String s) {

    }

    public void setFullName(String s) {

    }

    public void setLastLoginTime(Date t) {

    }

    public void setLastName(String s) {

    }

    public void setOrganizationId(String organizationId) {

    }

    public void setPassword(String s) {

    }

    public void setUserName(String s) {

    }

    public String getDisplayName() {      
      return null;
    }

    public void setDisplayName(String displayName) {
   
    }
    public boolean isEnabled() {

        return true;
    }
  }

  static class SimpleGroup implements Group {
    String id = null;

    public SimpleGroup(String id) {
      this.id = id;
    }

    public boolean equals(Object obj) {
      if (obj == null)
        return false;
      if (!(obj instanceof Group))
        return false;
      Group other = (Group) obj;
      return (id.equals(other.getId()));
    }

    public int hashCode() {
      return super.hashCode();
    }

    public String toString() {
      return getId();
    }

    public String getDescription() {
      return id;
    }

    public String getGroupName() {
      return id.substring(id.lastIndexOf('/') + 1);
    }

    public String getId() {
      return id;
    }

    public String getLabel() {
      return id;
    }

    public String getParentId() {
      return id.substring(0, id.lastIndexOf('/'));
    }

    public void setDescription(String desc) {

    }

    public void setGroupName(String name) {

    }

    public void setLabel(String name) {

    }

  }

  static class SimpleMembership implements Membership {
    String user;

    String group;

    String membershipType;

    public SimpleMembership(String user, String group, String membershipType) {
      this.user = user;
      this.group = group;
      this.membershipType = membershipType;
    }

    public boolean equals(Object obj) {
      if (obj == null)
        return false;
      if (!(obj instanceof Membership))
        return false;
      Membership other = (Membership) obj;
      return (user.equals(other.getUserName()) 
          && group.equals(other.getGroupId()) 
          && membershipType.equals(other.getMembershipType()));
    }

    public int hashCode() {
      return super.hashCode();
    }

    public String toString() {
      return getId();
    }

    public String getGroupId() {
      return group;
    }

    public String getId() {
      return user + "@" + membershipType + ":" + group;
    }

    public String getMembershipType() {
      return membershipType;
    }

    public String getUserName() {
      return user;
    }

    public void setMembershipType(String type) {

    }

  }

}
