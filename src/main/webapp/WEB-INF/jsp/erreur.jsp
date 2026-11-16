<%@ include file="fragments/header.jspf" %>
<div class="message error" role="alert">
  <strong>La demande n'a pas pu être traitée.</strong>
  <c:choose>
    <c:when test="${requestScope['jakarta.servlet.error.status_code'] == 403}"><p>Le formulaire a expiré ou son jeton de sécurité est invalide. Rechargez la page avant de réessayer.</p></c:when>
    <c:when test="${requestScope['jakarta.servlet.error.status_code'] == 409}"><p>Ce récapitulatif n'est plus à jour. Revenez à la simulation pour vérifier votre demande.</p></c:when>
    <c:when test="${requestScope['jakarta.servlet.error.status_code'] == 404}"><p>La page ou le produit demandé est introuvable.</p></c:when>
    <c:otherwise><p>La demande est invalide ou le service est indisponible. Aucune confirmation n'est émise sur cette page.</p></c:otherwise>
  </c:choose>
</div>
<div class="actions"><a class="button" href="<c:url value='/catalogue.do'/>">Retour au catalogue</a></div>
<%@ include file="fragments/footer.jspf" %>
