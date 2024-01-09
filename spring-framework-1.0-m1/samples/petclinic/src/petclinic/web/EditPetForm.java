/*
 * EditPetForm.java
 *
 */

package petclinic.web;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;

import petclinic.NoSuchEntityException;
import petclinic.Pet;

import org.springframework.web.bind.RequestUtils;
import org.springframework.web.servlet.ModelAndView;

/**
 *  JavaBean Form controller that is used to edit an existing <code>Pet</code>.
 *
 * @author  Ken Krebs
 */
public class EditPetForm extends AbstractClinicForm {
    
	public EditPetForm() {
		// need a session to hold the formBackingObject
		setSessionForm(true);
		// initialize the form from the formBackingObject 
		setBindOnNewForm(true);
	}
    
	/** Method updates an existing Pet. */
    protected ModelAndView onSubmit(Object command) throws ServletException {
    	// the edited object
        Pet editPet = (Pet) command;
        
        // get the original object
        Pet pet =  getClinic().findPet(editPet.getId());
        if(pet == null) {
            // should not happen unless id is corrupted
            throw new NoSuchEntityException(editPet);
        }
        
        // use the data from the edited object
		pet.copyPropertiesFrom(editPet);
        
        // delegate the update to the Business layer
        getClinic().update(pet);
        
        return new ModelAndView(getSuccessView(), "ownerId", Integer.toString(pet.getOwner().getId()));
    }
    
    /** Method forms a copy of an existing Pet for editing */
    protected Object formBackingObject(HttpServletRequest request) throws ServletException {
    	// get the Pet referred to by id in the request
        Pet pet =  getClinic().findPet(RequestUtils.getIntParameter(request, "petId", 0));
        if(pet == null) {
            throw new ServletException("petId missing from request on " + getClass());
        }
        
        //make a copy for editing
        Pet editPet = new Pet();
		editPet.copyPropertiesFrom(pet);
        return editPet;
    }
    
}
