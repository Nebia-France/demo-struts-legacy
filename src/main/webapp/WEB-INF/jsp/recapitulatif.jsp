<%@ include file="fragments/header.jspf" %>
<p class="intro">Vérifiez les informations ci-dessous avant de confirmer votre demande de souscription.</p>
<section class="panel">
  <h2>Produit et souscripteur <a class="section-link" href="<c:url value='/souscripteur.do'/>">Modifier</a></h2>
  <div class="panel-body"><table class="data-table details"><tbody>
    <tr><th scope="row">Produit sélectionné</th><td><c:out value="${product.name}"/> — <c:out value="${product.profile}"/></td></tr>
    <tr><th scope="row">Souscripteur</th><td><c:out value="${draft.subscriber.firstName}"/> <c:out value="${draft.subscriber.lastName}"/></td></tr>
    <tr><th scope="row">Adresse électronique</th><td><c:out value="${draft.subscriber.email}"/></td></tr>
    <tr><th scope="row">Date de naissance</th><td><c:out value="${draft.subscriber.birthDateLabel}"/></td></tr>
  </tbody></table></div>
</section>
<section class="panel"><h2>Votre projet d'épargne <a class="section-link" href="<c:url value='/simulation.do'/>">Modifier</a></h2><div class="panel-body">
  <c:set var="result" value="${draft.simulation}"/>
  <%@ include file="fragments/result.jspf" %>
</div></section>
<html:form action="/confirmer" method="post">
  <%@ include file="fragments/csrf.jspf" %>
  <input type="hidden" name="confirmationKey" value="<c:out value='${draft.confirmationKey}'/>">
  <div class="acceptance">
    <input type="checkbox" name="accepted" id="accepted" value="true" aria-describedby="accepted-error" aria-invalid="${not empty errors.accepted}" <c:if test="${subscriptionForm.accepted}">checked</c:if>>
    <label for="accepted">Je confirme l'exactitude des informations saisies et avoir pris connaissance des hypothèses de simulation et de l'absence de garantie de rendement.</label>
    <p class="field-error" id="accepted-error"><c:out value="${errors.accepted}"/></p>
  </div>
  <div class="actions"><a href="<c:url value='/simulation.do'/>">&lt; Précédent</a><button type="submit">Confirmer la souscription &gt;</button></div>
</html:form>
<%@ include file="fragments/footer.jspf" %>
