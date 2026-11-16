<%@ include file="fragments/header.jspf" %>
<c:if test="${param.notice == 'parcours'}">
  <div class="message notice" role="status">Votre parcours est incomplet ou votre session a expiré. Veuillez choisir un produit pour commencer une nouvelle demande.</div>
</c:if>
<p class="intro">Choisissez le placement qui correspond à votre projet, puis réalisez une simulation avant de compléter votre demande de souscription.</p>
<section class="panel">
  <h2>Nos solutions d'épargne</h2>
  <div class="panel-body">
    <p>Comparez les trois profils de placement et consultez leurs caractéristiques avant de faire votre choix.</p>
    <div class="table-scroll">
      <table class="data-table catalogue">
        <caption class="sr-only">Comparaison des trois placements</caption>
        <thead><tr><th scope="col">Placement</th><th scope="col">Profil de risque</th><th scope="col">Taux annuel<br>illustratif</th><th scope="col">À partir de</th><th scope="col">Information</th></tr></thead>
        <tbody>
          <c:forEach var="item" items="${products}">
            <c:url var="productUrl" value="/produit.do"><c:param name="code" value="${item.code}"/></c:url>
            <tr>
              <th scope="row"><a href="<c:out value='${productUrl}'/>"><c:out value="${item.name}"/></a></th>
              <td><c:out value="${item.profile}"/></td>
              <td class="numeric rate"><fmt:formatNumber value="${item.ratePercent}" maxFractionDigits="0"/> %</td>
              <td class="numeric">100,00 €</td>
              <td><a href="<c:out value='${productUrl}'/>">Consulter<span class="sr-only"> <c:out value="${item.name}"/></span> &gt;</a></td>
            </tr>
          </c:forEach>
        </tbody>
      </table>
    </div>
    <p class="fine-print">Les niveaux de risque sont indicatifs. Le capital et les rendements ne sont pas garantis.</p>
  </div>
</section>
<section class="panel">
  <h2>Comment souscrire ?</h2>
  <div class="panel-body">
    <ol class="instructions"><li>Consultez la fiche du produit de votre choix.</li><li>Renseignez vos informations et simulez votre épargne.</li><li>Vérifiez votre récapitulatif et confirmez votre demande de souscription.</li></ol>
  </div>
</section>
<div class="message notice"><strong>Information importante</strong><br>Les projections reposent sur les hypothèses de calcul indiquées. Les performances futures ne sont pas garanties.</div>
<%@ include file="fragments/footer.jspf" %>
