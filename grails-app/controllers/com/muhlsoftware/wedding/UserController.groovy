/*******************************************************************************
 Party Planner web application for guest seat assignments and entree choices
 Copyright (C) 2012  Aaron Mondelblatt
 This program is free software: you can redistribute it and/or modify
 it under the terms of the GNU General Public License as published by
 the Free Software Foundation, either version 3 of the License, or
 (at your option) any later version.
 This program is distributed in the hope that it will be useful,
 but WITHOUT ANY WARRANTY; without even the implied warranty of
 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 GNU General Public License for more details.
 You should have received a copy of the GNU General Public License
 along with this program.  If not, see http://www.gnu.org/licenses/gpl-3.0.txt.
 ***********************************************************************************/
package com.muhlsoftware.wedding

import grails.converters.JSON
import grails.util.GrailsNameUtils

import org.codehaus.groovy.grails.plugins.springsecurity.NullSaltSource
import org.codehaus.groovy.grails.plugins.springsecurity.SpringSecurityUtils
import org.springframework.dao.DataIntegrityViolationException
import com.muhlsoftware.wedding.extralogin.ClientAuthentication
import grails.plugins.springsecurity.Secured
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

class UserController extends grails.plugins.springsecurity.ui.UserController {
	def saltSource
	def userCache

	
	def create = {
		params.client = Client.findById(params.client)
		def user = lookupUserClass().newInstance(params)
		ClientAuthentication auth = SecurityContextHolder.getContext().getAuthentication();
		def client = Client.findById(auth.getClientId())
		[user: user, authorityList: sortedRoles(),client:client]
	}

	def save = {
		String passwordFieldName = SpringSecurityUtils.securityConfig.userLookup.passwordPropertyName
		params.client = Client.findById(params.client)
		def user = lookupUserClass().newInstance(params)
		if (params.password) {
			String salt = saltSource instanceof NullSaltSource ? null : params.username
			user."$passwordFieldName" = springSecurityUiService.encodePassword(params.password, salt)
		}
		if (!user.save(flush: true)) {
			render view: 'create', model: [user: user, authorityList: sortedRoles()]
			return
		}

		addRoles(user)
		flash.message = "${message(code: 'default.created.message', args: [message(code: 'user.label', default: 'User'), user.id])}"
		redirect action: edit, id: user.id
	}

	def edit = {
		String usernameFieldName = SpringSecurityUtils.securityConfig.userLookup.usernamePropertyName

		def user = params.username ? lookupUserClass().findWhere((usernameFieldName): params.username) : null
		if (!user) user = findById()
		if (!user) return

			return buildUserModel(user)
	}

	def update = {
		String passwordFieldName = SpringSecurityUtils.securityConfig.userLookup.passwordPropertyName

		def user = findById()
		if (!user) return
			if (!versionCheck('user.label', 'User', user, [user: user])) {
				return
			}

		def oldPassword = user."$passwordFieldName"
		params.client = Client.findById(params.client)
		user.properties = params
		if (params.password && !params.password.equals(oldPassword)) {
			String salt = saltSource instanceof NullSaltSource ? null : params.username
			user."$passwordFieldName" = springSecurityUiService.encodePassword(params.password, salt)
		}

		if (!user.save(flush: true)) {
			render view: 'edit', model: buildUserModel(user)
			return
		}

		String usernameFieldName = SpringSecurityUtils.securityConfig.userLookup.usernamePropertyName

		lookupUserRoleClass().removeAll user
		addRoles user
		userCache.removeUserFromCache user[usernameFieldName]
		flash.message = "${message(code: 'default.updated.message', args: [message(code: 'user.label', default: 'User'), user.id])}"
		redirect action: edit, id: user.id
	}

	def delete = {
		def user = findById()
		if (!user) return

			String usernameFieldName = SpringSecurityUtils.securityConfig.userLookup.usernamePropertyName
		try {
			lookupUserRoleClass().removeAll user
			user.delete flush: true
			userCache.removeUserFromCache user[usernameFieldName]
			flash.message = "${message(code: 'default.deleted.message', args: [message(code: 'user.label', default: 'User'), params.id])}"
			redirect action: search
		}
		catch (DataIntegrityViolationException e) {
			flash.error = "${message(code: 'default.not.deleted.message', args: [message(code: 'user.label', default: 'User'), params.id])}"
			redirect action: edit, id: params.id
		}
	}

	def search = {
		[enabled: 0, accountExpired: 0, accountLocked: 0, passwordExpired: 0]
	}

	def userSearch = {

		boolean useOffset = params.containsKey('offset')
		setIfMissing 'max', 10, 100
		setIfMissing 'offset', 0

		def hql = new StringBuilder('FROM ').append(lookupUserClassName()).append(' u WHERE 1=1 ')
		def queryParams = [:]

		def userLookup = SpringSecurityUtils.securityConfig.userLookup
		String usernameFieldName = userLookup.usernamePropertyName

		for (name in [username: usernameFieldName]) {
			if (params[name.key]) {
				hql.append " AND LOWER(u.${name.value}) LIKE :${name.key}"
				queryParams[name.key] = params[name.key].toLowerCase() + '%'
			}
		}

		String enabledPropertyName = userLookup.enabledPropertyName
		String accountExpiredPropertyName = userLookup.accountExpiredPropertyName
		String accountLockedPropertyName = userLookup.accountLockedPropertyName
		String passwordExpiredPropertyName = userLookup.passwordExpiredPropertyName

		for (name in [enabled: enabledPropertyName,
			accountExpired: accountExpiredPropertyName,
			accountLocked: accountLockedPropertyName,
			passwordExpired: passwordExpiredPropertyName]) {
			Integer value = params.int(name.key)
			if (value) {
				hql.append " AND u.${name.value}=:${name.key}"
				queryParams[name.key] = value == 1
			}
		}
			
	
		if(SpringSecurityUtils.ifNotGranted('ROLE_SUPER_USER')) {
			ClientAuthentication auth = SecurityContextHolder.getContext().getAuthentication();
			def client = Client.findById(auth.getClientId())
			hql.append " AND u.client=:client"
			queryParams.putAt("client",client)
		}
		int totalCount = lookupUserClass().executeQuery("SELECT COUNT(DISTINCT u) $hql", queryParams)[0]

		Integer max = params.int('max')
		Integer offset = params.int('offset')

		String orderBy = ''
		if (params.sort) {
			orderBy = " ORDER BY u.$params.sort ${params.order ?: 'ASC'}"
		}

		def results = lookupUserClass().executeQuery(
				"SELECT DISTINCT u $hql $orderBy",
				queryParams, [max: max, offset: offset])
		def model = [results: results, totalCount: totalCount, searched: true]

		// add query params to model for paging
		for (name in ['username', 'enabled', 'accountExpired', 'accountLocked',
			'passwordExpired', 'sort', 'order']) {
			model[name] = params[name]
		}

		render view: 'search', model: model
	}

	/**
	 * Ajax call used by autocomplete textfield.
	 */
	def ajaxUserSearch = {

		def jsonData = []

		if (params.term?.length() > 2) {
			String username = params.term
			String usernameFieldName = SpringSecurityUtils.securityConfig.userLookup.usernamePropertyName
			
			def client
			setIfMissing 'max', 10, 100
			def qp = [name: "${username.toLowerCase()}%"]
			def clientQuery = ''
			if(SpringSecurityUtils.ifNotGranted('ROLE_SUPER_USER')) {
				ClientAuthentication auth = SecurityContextHolder.getContext().getAuthentication();
				client = Client.findById(auth.getClientId())
				clientQuery = " and u.client = :client "
				qp.putAt("client",client)
			}
			def results = lookupUserClass().executeQuery(
					"SELECT DISTINCT u.$usernameFieldName " +
					"FROM ${lookupUserClassName()} u " +
					"WHERE LOWER(u.$usernameFieldName) LIKE :name " + clientQuery +
					"ORDER BY u.$usernameFieldName",
					qp,[max: params.max])

			for (result in results) {
				jsonData << [value: result]
			}
		}

		render text: jsonData as JSON, contentType: 'text/plain'
	}

	protected void addRoles(user) {
		String upperAuthorityFieldName = GrailsNameUtils.getClassName(
				SpringSecurityUtils.securityConfig.authority.nameField, null)
		String authorityFieldName = SpringSecurityUtils.securityConfig.authority.nameField
		for (String key in params.keySet()) {
			if (key.contains('ROLE') && 'on' == params.get(key)) {
				lookupUserRoleClass().create user, lookupRoleClass()."findBy$upperAuthorityFieldName"(key), true
			}
		}
	}

	protected Map buildUserModel(user) {

		String authorityFieldName = SpringSecurityUtils.securityConfig.authority.nameField
		String authoritiesPropertyName = SpringSecurityUtils.securityConfig.userLookup.authoritiesPropertyName

		List roles = sortedRoles()
		Set userRoleNames = user[authoritiesPropertyName].collect { it[authorityFieldName] }
		def granted = [:]
		def notGranted = [:]
		for (role in roles) {
			String authority = role[authorityFieldName]
			if (userRoleNames.contains(authority)) {
				granted[(role)] = userRoleNames.contains(authority)
			}
			else {
				notGranted[(role)] = userRoleNames.contains(authority)
			}
		}

		return [user: user, roleMap: granted + notGranted]
	}

	protected findById() {
		def user = lookupUserClass().get(params.id)
		if (!user) {
			flash.message = "${message(code: 'default.not.found.message', args: [message(code: 'user.label', default: 'User'), params.id])}"
			redirect action: search
		}

		user
	}

	protected List sortedRoles() {
		lookupRoleClass().list().sort { it.authority }
	}
}