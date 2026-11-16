<%@ include file="fragments/header.jspf" %>
<p class="intro">Consultez les caractéristiques de <strong><c:out value="${product.name}"/></strong> avant de commencer votre demande.</p>
<section class="panel">
  <h2><c:out value="${product.name}"/> — caractéristiques</h2>
  <div class="panel-body">
    <p><c:out value="${product.description}"/></p>
    <table class="data-table details">
      <tbody>
        <tr><th scope="row">Profil de risque</th><td><c:out value="${product.profile}"/></td></tr>
        <tr><th scope="row">Taux annuel nominal illustratif</th><td><strong><fmt:formatNumber value="${product.ratePercent}" maxFractionDigits="0"/> %</strong></td></tr>
        <tr><th scope="row">Versement initial</th><td>De 100,00 à 100 000,00 €</td></tr>
        <tr><th scope="row">Versements mensuels</th><td>De 0,00 à 2 000,00 €</td></tr>
        <tr><th scope="row">Durée simulée</th><td>De 1 à 30 ans</td></tr>
        <tr><th scope="row">Condition d'âge</th><td>Souscripteur majeur</td></tr>
      </tbody>
    </table>
    <p class="fine-print">La simulation repose sur un taux constant, sans garantie de capital ni de performance. Frais et fiscalité non pris en compte.</p>
  </div>
</section>
<html:form action="/commencer" method="post">
  <%@ include file="fragments/csrf.jspf" %>
  <input type="hidden" name="code" value="<c:out value='${product.code}'/>">
  <div class="actions"><a href="<c:url value='/catalogue.do'/>">&lt; Retour au catalogue</a><button type="submit">Souscrire à ce produit &gt;</button></div>
</html:form>
<%@ include file="fragments/footer.jspf" %>
