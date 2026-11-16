package fr.demo.souscription.action;

import fr.demo.souscription.model.Product;
import fr.demo.souscription.web.Application;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.util.Optional;
import org.apache.struts.action.Action;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;

public final class CatalogAction extends Action {
    @Override
    public ActionForward execute(ActionMapping mapping, ActionForm form,
                                 HttpServletRequest request, HttpServletResponse response) throws Exception {
        if ("POST".equals(request.getMethod())) { response.sendError(405); return null; }
        Application app = Application.get(getServlet().getServletContext());
        request.setAttribute("step", 1);
        if ("/produit".equals(mapping.getPath())) {
            Optional<Product> product = app.getCatalog().find(request.getParameter("code"));
            if (!product.isPresent()) { response.sendError(404); return null; }
            request.setAttribute("product", product.get());
            request.setAttribute("pageTitle", "Fiche produit");
            return mapping.findForward("produit");
        }
        request.setAttribute("products", app.getCatalog().getProducts());
        request.setAttribute("pageTitle", "Catalogue des placements");
        return mapping.findForward("catalogue");
    }
}
