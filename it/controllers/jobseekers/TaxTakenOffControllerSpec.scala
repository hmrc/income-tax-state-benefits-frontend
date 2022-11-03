
package controllers.jobseekers

import play.api.http.HeaderNames
import play.api.libs.ws.WSResponse
import support.IntegrationTest
import play.api.http.Status.{OK}
import support.builders.StateBenefitsUserDataBuilder.aStateBenefitsUserData

import java.util.UUID

class TaxTakenOffControllerSpec extends IntegrationTest {

  private def url(taxYear: Int, sessionDataId: UUID): String =
    s"/update-and-submit-income-tax-return/state-benefits/$taxYear/jobseekers-allowance/$sessionDataId/tax-taken-off"

  private val sessionDataId = UUID.randomUUID()

  ".show" should {
    "should render Tax Taken Off page for end of year" in {
      lazy val result: WSResponse = {
        authoriseAgentOrIndividual(isAgent = false)
        userSessionDataStub(sessionDataId, aStateBenefitsUserData)
        urlGet(url(taxYearEOY, sessionDataId), headers = Seq(HeaderNames.COOKIE -> playSessionCookies(taxYearEOY)))
      }
      result.status shouldBe OK
    }
  }

  ".submit" should {

  }
}