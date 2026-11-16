<%@ include file="fragments/header.jspf" %>
<div class="message success" role="status"><strong>Votre demande de souscription a bien été enregistrée.</strong></div>
<section class="panel"><h2>Accusé d'enregistrement</h2><div class="panel-body">
  <table class="data-table details"><tbody>
    <tr><th scope="row">Référence de la demande</th><td><strong id="reference"><c:out value="${subscription.reference}"/></strong></td></tr>
    <tr><th scope="row">Enregistrée le</th><td><c:out value="${subscription.createdAtLabel}"/> (Paris)</td></tr>
    <tr><th scope="row">Produit</th><td><c:out value="${subscription.product.name}"/></td></tr>
    <tr><th scope="row">Souscripteur</th><td><c:out value="${subscription.subscriber.firstName}"/> <c:out value="${subscription.subscriber.lastName}"/></td></tr>
  </tbody></table>
  <c:set var="result" value="${subscription.simulation}"/>
  <c:set var="product" value="${subscription.product}"/>
  <%@ include file="fragments/result.jspf" %>
</div></section>
<p>Conservez la référence de votre demande.</p>
<div class="actions"><a class="button" href="<c:url value='/catalogue.do'/>">Retour au catalogue</a></div>
<%@ include file="fragments/footer.jspf" %>
