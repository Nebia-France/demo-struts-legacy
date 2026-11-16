package fr.demo.souscription.web;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.regex.Pattern;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class SubscriptionIT {
    private final String base = System.getProperty("horizon.baseUrl", "http://127.0.0.1:18080/horizon");

    @Test
    void catalogueAndAllProductsRenderWithRealJakartaTaglibs() throws Exception {
        Client client = new Client();
        Page catalogue = client.get("/catalogue.do");
        assertEquals(200, catalogue.status);
        assertEquals(3, catalogue.document.select("table.catalogue tbody tr").size());
        assertTrue(catalogue.html.contains("Extranet Financière Horizon"));
        assertTrue(catalogue.html.contains("Horizon Équilibre"));
        assertTrue(catalogue.html.contains("réalisez une simulation"));
        assertTrue(catalogue.html.contains("Financière Horizon — Épargne"));
        assertEquals(0, catalogue.document.select(".demo-strip, .side-box").size());
        assertTrue(catalogue.html.contains("Les performances futures ne sont pas garanties."));
        assertEquals("no-store", catalogue.connection.getHeaderField("Cache-Control"));
        assertTrue(catalogue.connection.getHeaderField("Content-Security-Policy").contains("script-src 'none'"));
        assertTrue(catalogue.connection.getHeaderField("Set-Cookie").contains("HttpOnly"));
        assertTrue(catalogue.connection.getHeaderField("Set-Cookie").contains("SameSite=Lax"));
        for (String code : new String[] {"PRUDENT", "EQUILIBRE", "DYNAMIQUE"}) {
            Page product = client.get("/produit.do?code=" + code);
            assertEquals(200, product.status);
            assertFalse(product.csrf().isEmpty());
            assertEquals(1, product.document.select("form[action$='commencer.do']").size());
        }
        assertEquals(404, client.get("/produit.do?code=INCONNU").status);
        assertEquals(404, client.get("/WEB-INF/jsp/recapitulatif.jsp").status);
        assertEquals(404, client.get("/console").status);
    }

    @Test
    void completeJourneyEscapesUserDataAndPreventsDuplicateConfirmations() throws Exception {
        Client client = new Client();
        Page subscriber = start(client);
        Page simulation = client.follow(client.post("/souscripteur.do", fields(
                "csrf", subscriber.csrf(), "firstName", "Camille <em>Test</em>", "lastName", "Martin",
                "email", "camille@example.test", "birthDate", "01/01/1980")));
        assertTrue(simulation.html.contains("Simulation de votre placement"));
        Page calculated = client.follow(client.post("/simulation.do", fields(
                "csrf", simulation.csrf(), "initial", "1000,00", "monthly", "100,00",
                "years", "5", "command", "calculer")));
        assertEquals(1, calculated.document.select("#capital-final").size());
        Page recap = client.follow(client.post("/simulation.do", fields(
                "csrf", calculated.csrf(), "initial", "1000,00", "monthly", "100,00",
                "years", "5", "command", "continuer")));
        assertTrue(recap.html.contains("Camille &lt;em&gt;Test&lt;/em&gt;"));
        assertEquals(0, recap.document.select("em").size());
        Page back = client.get("/souscripteur.do");
        assertEquals("Camille <em>Test</em>", back.document.selectFirst("#firstName").val());
        Page backSimulation = client.follow(client.post("/souscripteur.do", fields(
                "csrf", back.csrf(), "firstName", "Camille <em>Test</em>", "lastName", "Martin",
                "email", "camille@example.test", "birthDate", "01/01/1980")));
        assertEquals("1000,00", backSimulation.document.selectFirst("#initial").val());
        assertEquals("100,00", backSimulation.document.selectFirst("#monthly").val());
        recap = client.get("/recapitulatif.do");
        String key = recap.document.selectFirst("[name=confirmationKey]").val();
        Map<String, String> confirmation = fields("csrf", recap.csrf(), "confirmationKey", key);
        Page rejected = client.post("/confirmer.do", confirmation);
        assertEquals(200, rejected.status);
        assertTrue(rejected.html.contains("Vous devez valider"));
        confirmation.put("accepted", "true");
        Page confirmed = client.follow(client.post("/confirmer.do", confirmation));
        assertEquals(200, confirmed.status);
        String reference = confirmed.document.selectFirst("#reference").text();
        assertTrue(reference.matches("[a-f0-9-]{36}"));
        Page duplicate = client.follow(client.post("/confirmer.do", confirmation));
        assertEquals(reference, duplicate.document.selectFirst("#reference").text());
        assertEquals(reference, client.get("/confirmation.do").document.selectFirst("#reference").text());
        Client stranger = new Client();
        assertEquals(302, stranger.get("/confirmation.do?reference=" + reference).status);
    }

    @Test
    void validatesInputsKeepsValuesAndBlocksStaleConfirmation() throws Exception {
        Client client = new Client();
        Page form = start(client);
        Page invalid = client.post("/souscripteur.do", fields("csrf", form.csrf(), "firstName", "",
                "lastName", "Martin", "email", "bad", "birthDate", "31/02/2000"));
        assertEquals(200, invalid.status);
        assertEquals("Martin", invalid.document.selectFirst("#lastName").val());
        assertEquals("true", invalid.document.selectFirst("#email").attr("aria-invalid"));
        assertTrue(invalid.document.selectFirst("#email").attr("aria-describedby").contains("email-error"));
        assertEquals(3, invalid.document.select(".error li").size());
        assertTrue(client.get("/recapitulatif.do").location().endsWith("/souscripteur.do"));
        Page simulation = client.follow(client.post("/souscripteur.do", fields("csrf", invalid.csrf(),
                "firstName", "Camille", "lastName", "Martin", "email", "camille@example.test",
                "birthDate", "01/01/1980")));
        Page badAmounts = client.post("/simulation.do", fields("csrf", simulation.csrf(),
                "initial", "-1", "monthly", "1,001", "years", "31", "command", "continuer"));
        assertEquals(3, badAmounts.document.select(".error li").size());
        Page recap = client.follow(client.post("/simulation.do", fields("csrf", simulation.csrf(),
                "initial", "100", "monthly", "0", "years", "1", "command", "continuer")));
        String oldKey = recap.document.selectFirst("[name=confirmationKey]").val();
        client.follow(client.post("/simulation.do", fields("csrf", recap.csrf(),
                "initial", "200", "monthly", "0", "years", "1", "command", "continuer")));
        Page stale = client.post("/confirmer.do", fields("csrf", recap.csrf(),
                "confirmationKey", oldKey, "accepted", "true"));
        assertEquals(409, stale.status);
        assertFalse(stale.html.contains("Exception"));
    }

    @Test
    void rejectsForgedPostsAndExplainsMissingSession() throws Exception {
        Client client = new Client();
        Page product = client.get("/produit.do?code=PRUDENT");
        assertEquals(403, client.post("/commencer.do", fields("code", "PRUDENT")).status);
        assertEquals(403, client.post("/commencer.do", fields("csrf", "forged", "code", "PRUDENT")).status);
        assertEquals(400, client.post("/commencer.do", fields("csrf", product.csrf(), "code", "BAD")).status);
        assertEquals(405, client.get("/commencer.do").status);
        Client expired = new Client();
        Page response = expired.get("/recapitulatif.do");
        assertEquals(302, response.status);
        assertTrue(expired.follow(response).html.contains("votre session a expiré"));
        assertEquals(403, expired.post("/confirmer.do", fields("csrf", product.csrf())).status);
    }

    private Page start(Client client) throws Exception {
        Page product = client.get("/produit.do?code=EQUILIBRE");
        return client.follow(client.post("/commencer.do", fields("csrf", product.csrf(), "code", "EQUILIBRE")));
    }

    private static Map<String, String> fields(String... pairs) {
        Map<String, String> fields = new LinkedHashMap<>();
        for (int i = 0; i < pairs.length; i += 2) { fields.put(pairs[i], pairs[i + 1]); }
        return fields;
    }

    private final class Client {
        private String cookie;
        Page get(String path) throws Exception { return request("GET", path, null); }
        Page post(String path, Map<String, String> values) throws Exception { return request("POST", path, values); }
        Page follow(Page page) throws Exception {
            assertEquals(302, page.status, page.document.text());
            return get(page.location());
        }

        private Page request(String method, String path, Map<String, String> values) throws Exception {
            URL url = new URL(path.startsWith("http") ? path : path.startsWith("/horizon/")
                    ? new URL(base).getProtocol() + "://" + new URL(base).getAuthority() + path : base + path);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setInstanceFollowRedirects(false);
            connection.setRequestMethod(method);
            connection.setConnectTimeout(10000);
            connection.setReadTimeout(10000);
            if (cookie != null) { connection.setRequestProperty("Cookie", cookie); }
            if (values != null) {
                StringBuilder body = new StringBuilder();
                for (Map.Entry<String, String> field : values.entrySet()) {
                    if (body.length() > 0) { body.append('&'); }
                    body.append(URLEncoder.encode(field.getKey(), "UTF-8")).append('=')
                            .append(URLEncoder.encode(field.getValue(), "UTF-8"));
                }
                connection.setDoOutput(true);
                connection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded; charset=UTF-8");
                try (java.io.OutputStream stream = connection.getOutputStream()) {
                    stream.write(body.toString().getBytes(StandardCharsets.UTF_8));
                }
            }
            int status = connection.getResponseCode();
            String setCookie = connection.getHeaderField("Set-Cookie");
            if (setCookie != null && setCookie.startsWith("JSESSIONID=")) { cookie = setCookie.split(";")[0]; }
            ByteArrayOutputStream bytes = new ByteArrayOutputStream();
            try (InputStream stream = status >= 400 ? connection.getErrorStream() : connection.getInputStream()) {
                if (stream != null) {
                    byte[] buffer = new byte[4096];
                    int count;
                    while ((count = stream.read(buffer)) != -1) { bytes.write(buffer, 0, count); }
                }
            }
            return new Page(status, new String(bytes.toByteArray(), StandardCharsets.UTF_8), connection);
        }
    }

    private static final class Page {
        private static final Pattern DEMO_NOTICE = Pattern.compile(
                "\\b(?:d[eé]mo\\p{L}*|ficti\\p{L}*|p[eé]dagog\\p{L}*)\\b",
                Pattern.CASE_INSENSITIVE | Pattern.UNICODE_CASE);
        private final int status;
        private final String html;
        private final Document document;
        private final HttpURLConnection connection;
        Page(int status, String html, HttpURLConnection connection) {
            this.status = status;
            this.html = html;
            this.document = Jsoup.parse(html);
            this.connection = connection;
            assertFalse(DEMO_NOTICE.matcher(document.text()).find(),
                    "Une notice de démonstration subsiste dans la page : " + connection.getURL());
            assertFalse(document.text().contains("Application Struts 1 / JSP"));
            assertEquals(0, document.select(".tabs-note").size());
        }
        String csrf() { return document.selectFirst("input[name=csrf]").val(); }
        String location() { return connection.getHeaderField("Location"); }
    }
}
