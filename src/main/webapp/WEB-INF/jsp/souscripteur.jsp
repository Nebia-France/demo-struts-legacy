<%@ include file="fragments/header.jspf" %>
<p class="intro">Produit sélectionné : <strong><c:out value="${product.name}"/></strong>. Renseignez les informations du souscripteur.</p>
<html:form action="/souscripteur" method="post">
  <%@ include file="fragments/csrf.jspf" %>
  <section class="panel">
    <h2>Vos informations personnelles</h2>
    <div class="panel-body">
      <p class="required-legend"><span class="required">*</span> Tous les champs sont obligatoires.</p>
      <div class="form-row">
        <label for="firstName">Prénom <span class="required">*</span></label>
        <div><input type="text" name="firstName" id="firstName" maxlength="80" required value="<c:out value='${subscriptionForm.firstName}'/>" aria-describedby="firstName-error" aria-invalid="${not empty errors.firstName}"><p class="field-error" id="firstName-error"><c:out value="${errors.firstName}"/></p></div>
      </div>
      <div class="form-row">
        <label for="lastName">Nom <span class="required">*</span></label>
        <div><input type="text" name="lastName" id="lastName" maxlength="80" required value="<c:out value='${subscriptionForm.lastName}'/>" aria-describedby="lastName-error" aria-invalid="${not empty errors.lastName}"><p class="field-error" id="lastName-error"><c:out value="${errors.lastName}"/></p></div>
      </div>
      <div class="form-row">
        <label for="email">Adresse électronique <span class="required">*</span></label>
        <div><input type="text" name="email" id="email" maxlength="254" required value="<c:out value='${subscriptionForm.email}'/>" aria-describedby="email-hint email-error" aria-invalid="${not empty errors.email}"><p class="hint" id="email-hint">Exemple : prenom.nom@example.com</p><p class="field-error" id="email-error"><c:out value="${errors.email}"/></p></div>
      </div>
      <div class="form-row">
        <label for="birthDate">Date de naissance <span class="required">*</span></label>
        <div><input type="text" name="birthDate" id="birthDate" maxlength="10" size="12" required value="<c:out value='${subscriptionForm.birthDate}'/>" aria-describedby="birthDate-hint birthDate-error" aria-invalid="${not empty errors.birthDate}"><p class="hint" id="birthDate-hint">JJ/MM/AAAA — réservé aux personnes de 18 ans et plus.</p><p class="field-error" id="birthDate-error"><c:out value="${errors.birthDate}"/></p></div>
      </div>
    </div>
  </section>
  <div class="actions"><a href="<c:url value='/catalogue.do'/>">&lt; Changer de produit</a><button type="submit">Continuer &gt;</button></div>
</html:form>
<%@ include file="fragments/footer.jspf" %>
