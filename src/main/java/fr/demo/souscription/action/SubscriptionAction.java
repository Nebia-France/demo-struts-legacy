package fr.demo.souscription.action;

import fr.demo.souscription.form.SubscriptionForm;
import fr.demo.souscription.model.Draft;
import fr.demo.souscription.model.Product;
import fr.demo.souscription.model.Simulation;
import fr.demo.souscription.model.Subscriber;
import fr.demo.souscription.model.Subscription;
import fr.demo.souscription.web.Application;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.sql.SQLException;
import java.util.Collections;
import java.util.Map;
import java.util.Optional;
import org.apache.struts.action.Action;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;

public final class SubscriptionAction extends Action {
    @Override
    public ActionForward execute(ActionMapping mapping, ActionForm actionForm,
                                 HttpServletRequest request, HttpServletResponse response) throws Exception {
        HttpSession session = request.getSession();
        Application app = Application.get(getServlet().getServletContext());
        SubscriptionForm form = (SubscriptionForm) actionForm;
        synchronized (session) {
            String path = mapping.getPath();
            boolean post = "POST".equals(request.getMethod());
            if ("/commencer".equals(path)) {
                if (!post) { response.sendError(405); return null; }
                Optional<Product> product = app.getCatalog().find(request.getParameter("code"));
                if (!product.isPresent()) { response.sendError(400); return null; }
                session.setAttribute("draft", new Draft(product.get()));
                session.removeAttribute("completed");
                return redirect("/souscripteur.do");
            }
            if ("/confirmation".equals(path)) {
                if (post) { response.sendError(405); return null; }
                Subscription completed = (Subscription) session.getAttribute("completed");
                if (completed == null) { return restart(); }
                request.setAttribute("subscription", completed);
                return view(mapping, request, "confirmation", "Confirmation de la souscription", 5);
            }
            Draft draft = (Draft) session.getAttribute("draft");
            if (draft == null) {
                if ("/confirmer".equals(path) && post && session.getAttribute("completed") != null) {
                    Subscription completed = (Subscription) session.getAttribute("completed");
                    if (completed.getReference().equals(session.getAttribute("completedReference"))
                            && request.getParameter("confirmationKey") != null
                            && request.getParameter("confirmationKey").equals(session.getAttribute("completedKey"))) {
                        return redirect("/confirmation.do");
                    }
                }
                return restart();
            }
            request.setAttribute("draft", draft);
            request.setAttribute("product", draft.getProduct());
            if ("/souscripteur".equals(path)) {
                if (post) {
                    Map<String, String> errors = app.getValidator().subscriberErrors(form);
                    if (errors.isEmpty()) {
                        draft.setSubscriber(app.getValidator().subscriber(form));
                        return redirect("/simulation.do");
                    }
                    request.setAttribute("errors", errors);
                } else if (draft.getSubscriber() != null) {
                    Subscriber subscriber = draft.getSubscriber();
                    form.setFirstName(subscriber.getFirstName());
                    form.setLastName(subscriber.getLastName());
                    form.setEmail(subscriber.getEmail());
                    form.setBirthDate(subscriber.getBirthDateLabel());
                }
                return view(mapping, request, "souscripteur", "Identification du souscripteur", 2);
            }
            if (draft.getSubscriber() == null) { return redirect("/souscripteur.do"); }
            if ("/simulation".equals(path)) {
                if (post) {
                    Map<String, String> errors = app.getValidator().simulationErrors(form);
                    if (!"calculer".equals(request.getParameter("command"))
                            && !"continuer".equals(request.getParameter("command"))) {
                        response.sendError(400); return null;
                    }
                    if (errors.isEmpty()) {
                        draft.setSimulation(app.getSimulations().calculate(
                                app.getValidator().amount(form.getInitial()),
                                app.getValidator().amount(form.getMonthly()), Integer.parseInt(form.getYears()),
                                draft.getProduct().getAnnualRate()));
                        return redirect("continuer".equals(request.getParameter("command"))
                                ? "/recapitulatif.do" : "/simulation.do");
                    }
                    request.setAttribute("errors", errors);
                } else if (draft.getSimulation() != null) {
                    populateSimulation(form, draft.getSimulation());
                }
                return view(mapping, request, "simulation", "Simulation de votre placement", 3);
            }
            if (draft.getSimulation() == null) { return redirect("/simulation.do"); }
            if ("/recapitulatif".equals(path)) {
                if (post) { response.sendError(405); return null; }
                return view(mapping, request, "recapitulatif", "Récapitulatif de la demande", 4);
            }
            if ("/confirmer".equals(path)) {
                if (!post) { response.sendError(405); return null; }
                if (!draft.getConfirmationKey().equals(request.getParameter("confirmationKey"))) {
                    response.sendError(409); return null;
                }
                if (!form.isAccepted()) {
                    request.setAttribute("errors", Collections.singletonMap("accepted",
                            "Vous devez valider les informations et les hypothèses de simulation."));
                    return view(mapping, request, "recapitulatif", "Récapitulatif de la demande", 4);
                }
                try {
                    Subscription completed = app.getSubscriptions().confirm(draft);
                    session.setAttribute("completed", completed);
                    session.setAttribute("completedReference", completed.getReference());
                    session.setAttribute("completedKey", draft.getConfirmationKey());
                    session.removeAttribute("draft");
                    return redirect("/confirmation.do");
                } catch (SQLException ex) {
                    getServlet().getServletContext().log("Enregistrement refusé ; SQLState=" + ex.getSQLState());
                    response.setStatus(503);
                    request.setAttribute("errors", Collections.singletonMap("storage",
                            "L'enregistrement est momentanément indisponible. Aucune confirmation n'a été émise. "
                            + "Votre saisie est conservée ; vous pouvez réessayer."));
                    return view(mapping, request, "recapitulatif", "Récapitulatif de la demande", 4);
                }
            }
            response.sendError(404);
            return null;
        }
    }

    private void populateSimulation(SubscriptionForm form, Simulation simulation) {
        form.setInitial(simulation.getInitial().toPlainString().replace('.', ','));
        form.setMonthly(simulation.getMonthly().toPlainString().replace('.', ','));
        form.setYears(Integer.toString(simulation.getYears()));
    }

    private ActionForward view(ActionMapping mapping, HttpServletRequest request,
                               String name, String title, int step) {
        request.setAttribute("pageTitle", title);
        request.setAttribute("step", step);
        return mapping.findForward(name);
    }

    private ActionForward redirect(String path) { return new ActionForward(path, true); }
    private ActionForward restart() { return redirect("/catalogue.do?notice=parcours"); }
}
