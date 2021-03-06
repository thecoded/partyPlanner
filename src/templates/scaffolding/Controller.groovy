<%=packageName ? "package ${packageName}\n\n" : ''%>import org.springframework.dao.DataIntegrityViolationException
import grails.plugins.springsecurity.Secured
import com.muhlsoftware.wedding.extralogin.ClientAuthentication
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

@Secured(['ROLE_CLIENT_ADMIN'])
class ${className}Controller {

    static allowedMethods = [save: "POST", update: "POST", delete: "POST"]

    def index() {
        redirect(action: "list", params: params)
    }

    def list(Integer max) {
		
		params.max = Math.min(max ?: 10, 100)
		def c = ${className}.createCriteria()
		def results = c.list (params) {
			if(${className}.hasProperty("client")) {
				ClientAuthentication auth = SecurityContextHolder.getContext().getAuthentication();
				def client = Client.findById(auth.getClientId())
				eq("client":client) 
			}
		}
		
       
        [${propertyName}List: results, ${propertyName}Total:results.totalCount]
    }

    def create() {
		ClientAuthentication auth = SecurityContextHolder.getContext().getAuthentication();
        [${propertyName}: new ${className}(params),'auth':auth]
    }
	
    def save() {
		ClientAuthentication auth = SecurityContextHolder.getContext().getAuthentication();
        def ${propertyName} = new ${className}(params)
        if (!${propertyName}.save(flush: true)) {
			if(${propertyName} instanceof String) {
				${propertyName} = ${propertyName?.trim()}
			}
            render(view: "create", model: [${propertyName}: ${propertyName},'auth':auth])
            return
        }

        flash.message = message(code: 'default.created.message', args: [message(code: '${domainClass.propertyName}.label', default: '${className}'), ${propertyName}.id])
        redirect(action: "show", id: ${propertyName}.id)
    }

    def show(Long id) {
        def ${propertyName} = ${className}.get(id)
        if (!${propertyName}) {
            flash.message = message(code: 'default.not.found.message', args: [message(code: '${domainClass.propertyName}.label', default: '${className}'), id])
            redirect(action: "list")
            return
        }

        [${propertyName}: ${propertyName}]
    }

    def edit(Long id) {
		ClientAuthentication auth = SecurityContextHolder.getContext().getAuthentication();
        def ${propertyName} = ${className}.get(id)
        if (!${propertyName}) {
            flash.message = message(code: 'default.not.found.message', args: [message(code: '${domainClass.propertyName}.label', default: '${className}'), id])
            redirect(action: "list")
            return
        }

        [${propertyName}: ${propertyName}, 'auth':auth]
    }

    def update(Long id, Long version) {
        def ${propertyName} = ${className}.get(id)
        if (!${propertyName}) {
            flash.message = message(code: 'default.not.found.message', args: [message(code: '${domainClass.propertyName}.label', default: '${className}'), id])
            redirect(action: "list")
            return
        }
		
		ClientAuthentication auth = SecurityContextHolder.getContext().getAuthentication();

        if (version != null) {
            if (${propertyName}.version > version) {<% def lowerCaseName = grails.util.GrailsNameUtils.getPropertyName(className) %>
                ${propertyName}.errors.rejectValue("version", "default.optimistic.locking.failure",
                          [message(code: '${domainClass.propertyName}.label', default: '${className}')] as Object[],
                          "Another user has updated this ${className} while you were editing")
                render(view: "edit", model: [${propertyName}: ${propertyName},'auth':auth])
                return
            }
        }

        ${propertyName}.properties = params

        if (!${propertyName}.save(flush: true)) {
            render(view: "edit", model: [${propertyName}: ${propertyName}])
            return
        }

        flash.message = message(code: 'default.updated.message', args: [message(code: '${domainClass.propertyName}.label', default: '${className}'), ${propertyName}.id])
        redirect(action: "show", id: ${propertyName}.id)
    }

    def delete(Long id) {
        def ${propertyName} = ${className}.get(id)
        if (!${propertyName}) {
            flash.message = message(code: 'default.not.found.message', args: [message(code: '${domainClass.propertyName}.label', default: '${className}'), id])
            redirect(action: "list")
            return
        }

        try {
            ${propertyName}.delete(flush: true)
            flash.message = message(code: 'default.deleted.message', args: [message(code: '${domainClass.propertyName}.label', default: '${className}'), id])
            redirect(action: "list")
        }
        catch (DataIntegrityViolationException e) {
            flash.message = message(code: 'default.not.deleted.message', args: [message(code: '${domainClass.propertyName}.label', default: '${className}'), id])
            redirect(action: "show", id: id)
        }
    }
}
