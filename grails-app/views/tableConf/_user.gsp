<%
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
 %>
<g:set var="userTitle"><wed:formatGuestTitle guest='${guest}' /></g:set>
<div id="guest_id_${guest?.id}" ${guest?.isAttending  ?   "attending='attend'" : ''}  relGid="${guest?.guest?.id}"  guest="${guest?.id}" class="guest">${guest?.guest?.toString()} 
	<span>
		<span class="ui-state-default ui-corner-all icon-only-holder moreinfo" title="${userTitle}"><span class="ui-icon ui-icon-info"></span></span>
		<span class="ui-state-error ui-corner-all delete-user icon-only-holder" user="${guest?.id}" title="Delete User"><span class="ui-icon ui-icon-circle-minus"></span></span>
		<span class="ui-state-default ui-corner-all icon-only-holder edit-user" user="${guest?.id}" title="Edit User"><span class="ui-icon ui-icon-pencil"></span></span>
	</span>
	<div class="clear"></div>
</div>