/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.livy.server

import org.scalatest.{FunSuite, Matchers}

import org.apache.livy.{LivyBaseUnitTestSuite, LivyConf}

class AccessManagerSuite extends FunSuite with Matchers with LivyBaseUnitTestSuite {
  import LivyConf._

  private val viewUsers = Seq("user1", "user2", "user3")
  private val modifyUsers = Seq("user4", "user5")
  private val superUsers = Seq("user6", "user7")
  private val allowedUsers = Seq("user8", "user9")

  test("access permission") {
    val conf = new LivyConf()
      .set(ACCESS_CONTROL_ENABLED, true)
      .set(ACCESS_CONTROL_VIEW_USERS, viewUsers.mkString(","))
      .set(ACCESS_CONTROL_MODIFY_USERS, modifyUsers.mkString(","))
      .set(SUPERUSERS, superUsers.mkString(","))

    val accessManager = new AccessManager(conf)
    accessManager.isAccessControlOn should be (true)

    // check view access
    viewUsers.foreach { u => accessManager.checkViewPermissions(u) should be (true) }
    modifyUsers.foreach { u => accessManager.checkViewPermissions(u) should be (true) }
    superUsers.foreach { u => accessManager.checkViewPermissions(u) should be (true) }
    allowedUsers.foreach { u => accessManager.checkViewPermissions(u) should be (false) }

    accessManager.checkViewPermissions(null) should be (true)
    accessManager.checkViewPermissions("user8") should be (false)

    // check modify access
    viewUsers.foreach { u => accessManager.checkModifyPermissions(u) should be (false) }
    modifyUsers.foreach { u => accessManager.checkModifyPermissions(u) should be (true) }
    superUsers.foreach { u => accessManager.checkModifyPermissions(u) should be (true) }
    allowedUsers.foreach { u => accessManager.checkModifyPermissions(u) should be (false) }

    accessManager.checkModifyPermissions(null) should be (true)
    accessManager.checkModifyPermissions("user8") should be (false)

    // check super access
    viewUsers.foreach { u => accessManager.checkSuperUser(u) should be (false) }
    modifyUsers.foreach { u => accessManager.checkSuperUser(u) should be (false) }
    superUsers.foreach { u => accessManager.checkSuperUser(u) should be (true) }
    allowedUsers.foreach { u => accessManager.checkSuperUser(u) should be (false) }

    accessManager.checkSuperUser(null) should be (true)
    accessManager.checkSuperUser("user8") should be (false)
  }

  test("wildcard access permission") {
    val conf = new LivyConf()
      .set(ACCESS_CONTROL_ENABLED, true)
      .set(ACCESS_CONTROL_VIEW_USERS, "*")
      .set(ACCESS_CONTROL_MODIFY_USERS, "*")
      .set(SUPERUSERS, "*")

    val accessManager = new AccessManager(conf)
    accessManager.isAccessControlOn should be (true)

    accessManager.checkViewPermissions("anyUser") should be (true)
    accessManager.checkModifyPermissions("anyUser") should be (true)
    accessManager.checkSuperUser("anyUser") should be (true)
  }

  test("default allowed users") {
    val conf = new LivyConf()
      .set(ACCESS_CONTROL_ENABLED, true)
      .set(ACCESS_CONTROL_VIEW_USERS, viewUsers.mkString(","))
      .set(ACCESS_CONTROL_MODIFY_USERS, modifyUsers.mkString(","))
      .set(SUPERUSERS, superUsers.mkString(","))

    val accessManager = new AccessManager(conf)

    // check if configured users are allowed
    viewUsers.foreach { u => accessManager.isUserAllowed(u) should be (true) }
    modifyUsers.foreach { u => accessManager.isUserAllowed(u) should be (true) }
    superUsers.foreach { u => accessManager.isUserAllowed(u) should be (true) }

    accessManager.isUserAllowed("anyUser") should be (true)
    accessManager.isUserAllowed(null) should be (true)
  }

  test("configured users are not in the allowed list") {
    val conf = new LivyConf()
      .set(ACCESS_CONTROL_ENABLED, true)
      .set(ACCESS_CONTROL_VIEW_USERS, viewUsers.mkString(","))
      .set(ACCESS_CONTROL_MODIFY_USERS, modifyUsers.mkString(","))
      .set(SUPERUSERS, superUsers.mkString(","))
      .set(ACCESS_CONTROL_ALLOWED_USERS, allowedUsers.mkString(","))

    val accessManager = new AccessManager(conf)
    viewUsers.foreach { u => accessManager.isUserAllowed(u) should be (true) }
    modifyUsers.foreach { u => accessManager.isUserAllowed(u) should be (true) }
    superUsers.foreach { u => accessManager.isUserAllowed(u) should be (true) }
    allowedUsers.foreach { u => accessManager.isUserAllowed(u) should be (true) }

    accessManager.isUserAllowed("anyUser") should be (false)
  }
}
