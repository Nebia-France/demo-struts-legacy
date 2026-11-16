<%@ include file="fragments/header.jspf" %>
<p class="intro">Définissez votre projet pour <strong><c:out value="${product.name}"/></strong>. Vous pouvez recalculer la simulation avant de poursuivre.</p>
<html:form action="/simulation" method="post">
  <%@ include file="fragments/csrf.jspf" %>
  <section class="panel">
    <h2>Paramètres du placement</h2>
    <div class="panel-body">
      <p class="required-legend"><span class="required">*</span> Tous les champs sont obligatoires. Saisissez les montants sans séparateur de milliers.</p>
      <div class="form-row"><label for="initial">Versement initial <span class="required">*</span></label><div><input type="text" name="initial" id="initial" size="14" maxlength="9" inputmode="decimal" required value="<c:out value='${subscriptionForm.initial}'/>" aria-describedby="initial-hint initial-error" aria-invalid="${not empty errors.initial}"> €<p class="hint" id="initial-hint">De 100,00 à 100 000,00 €.</p><p class="field-error" id="initial-error"><c:out value="${errors.initial}"/></p></div></div>
      <div class="form-row"><label for="monthly">Versement mensuel <span class="required">*</span></label><div><input type="text" name="monthly" id="monthly" size="14" maxlength="9" inputmode="decimal" required value="<c:out value='${subscriptionForm.monthly}'/>" aria-describedby="monthly-hint monthly-error" aria-invalid="${not empty errors.monthly}"> €<p class="hint" id="monthly-hint">De 0,00 à 2 000,00 €, en fin de mois.</p><p class="field-error" id="monthly-error"><c:out value="${errors.monthly}"/></p></div></div>
      <div class="form-row"><label for="years">Durée <span class="required">*</span></label><div><input type="text" name="years" id="years" size="4" maxlength="2" inputmode="numeric" required value="<c:out value='${subscriptionForm.years}'/>" aria-describedby="years-hint years-error" aria-invalid="${not empty errors.years}"> an(s)<p class="hint" id="years-hint">Un nombre entier de 1 à 30 ans.</p><p class="field-error" id="years-error"><c:out value="${errors.years}"/></p></div></div>
      <div class="inline-actions"><button type="submit" name="command" value="calculer">Recalculer la simulation</button></div>
    </div>
  </section>
  <c:if test="${not empty draft.simulation and empty errors}">
    <section class="panel"><h2>Votre estimation</h2><div class="panel-body">
      <c:set var="result" value="${draft.simulation}"/>
      <%@ include file="fragments/result.jspf" %>
    </div></section>
  </c:if>
  <div class="actions"><a href="<c:url value='/souscripteur.do'/>">&lt; Précédent</a><button type="submit" name="command" value="continuer">Continuer &gt;</button></div>
</html:form>
<%@ include file="fragments/footer.jspf" %>
