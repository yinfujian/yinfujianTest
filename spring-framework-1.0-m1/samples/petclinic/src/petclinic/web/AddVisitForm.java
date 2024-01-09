/*
 * AddVisitForm.java
 *
 */

package petclinic.web;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import petclinic.Pet;
import petclinic.Visit;

import org.springframework.web.bind.RequestUtils;
import org.springframework.web.servlet.ModelAndView;

/**
 *  JavaBean Form controller that is used to add a new <code>Visit</code> to the system.
 *
 *  @author  Ken Krebs
 */
public class AddVisitForm extends AbstractClinicForm {
    
	public AddVisitForm() {
		// need a session to hold the formBackingObject
		setSessionForm(true);
	}
    
	/** Method inserts a new <code>Visit</code>. */
    protected ModelAndView onSubmit(Object command) throws ServletException {
        Visit visit = (Visit) command;
        
		// delegate the insert to the Business layer
        getClinic().insert(visit);
        
        return new ModelAndView(getSuccessView(), "ownerId", Integer.toString(visit.getPet().getOwner().getId()));
    }
    
    /** Method creates a new <code>Visit</code> with the correct <code>Pet</code> info */
    protected Object formBackingObject(HttpServletRequest request) throws ServletException {
        Pet pet =  getClinic().findPet(RequestUtils.getIntParameter(request, "petId", 0));
        if(pet == null)
            throw new ServletException("petId missing from request on " + getClass());
        Visit visit = new Visit();
        visit.setPet(pet);
        return visit;
    }
    
    protected ModelAndView handleInvalidSubmit(HttpServletRequest request, HttpServletResponse response)
    throws ServletException, IOException {
        return disallowDuplicateFormSubmission(request, response);
    }
    
}
