
# income-tax-state-benefits

This is where we make API calls from users viewing and making changes to the State Benefits section of their income tax return.

## Running the service locally

You will need to have the following:
- Installed [MongoDB](https://docs.mongodb.com/manual/installation/)
- Installed/configured [service manager](https://github.com/hmrc/service-manager)

This can be found in the [developer handbook](https://docs.tax.service.gov.uk/mdtp-handbook/documentation/developer-set-up/)

The service manager profile for this service is:

    sm2 --start INCOME_TAX_STATE_BENEFITS_FRONTEND

Run the following command to start the remaining services locally:

    sm2 --start INCOME_TAX_SUBMISSION_ALL

This service runs on port: `localhost:9376`

To run the service locally:

    sudo mongod (If not already running)
    sm2 --start INCOME_TAX_SUBMISSION_ALL
    sm2 --stop INCOME_TAX_STATE_BENEFITS_FRONTEND
    ./run.sh **OR** sbt -Dplay.http.router=testOnlyDoNotUseInAppConf.Routes run

To test the branch you're working on locally. You will need to run `sm2 --stop INCOME_TAX_STATE_BENEFITS_FRONTEND` followed by
`./run.sh`

### Running Tests

- Run Unit Tests:  `sbt test`
- Run Integration Tests: `sbt it/test`
- Run Unit and Integration Tests: `sbt test it/test`
- Run Unit and Integration Tests with coverage report: `./check.sh`<br/>
  which runs `sbt clean coverage test it/test coverageReport dependencyUpdates`

### Feature Switches

| Feature                         | Description                                                         |
|---------------------------------|---------------------------------------------------------------------|
| taxYearErrorFeatureSwitch       | Nothing (to be removed)                                             |
| Welsh Language                  | Enables a toggle to allow the user to change language to/from Welsh |
| sectionCompletedQuestionEnabled | Redirects user to 'Have You Completed' this section from CYA page   |
| sessionCookieServiceEnabled     | Retrieves session data from V&C when enabled                        |

### State Benefits Sources (HMRC-Held and Customer Data)
State Benefits data can come from different sources: HMRC-Held and Customer. HMRC-Held data is state benefits data that HMRC have for the user within the tax year, prior to any updates made by the user. The state benefits data displayed in-year is HMRC-Held.

Customer data is provided by the user. At the end of the tax year, users can view any existing state benefits data and make changes (create, update and delete).

Examples of the prior user data can be found here in the [income-tax-submission-stub](https://github.com/hmrc/income-tax-submission-stub/blob/main/app/models/StateBenefitsUsers.scala)

## Ninos with stub data for State Benefits

### In-Year
| Nino      | State Benefits data                 | Source   |
|-----------|-------------------------------------|----------|
| AC160000B | State Benefits user with Claim data | Customer |

### End of Year
| Nino      | State Benefits data                 | Source   |
|-----------|-------------------------------------|----------|
| AC160000B | State Benefits user with Claim data | Customer |


## Local

### Individual
* Login via: [http://localhost:9949/auth-login-stub/gg-sign-in](http://localhost:9949/auth-login-stub/gg-sign-in)
* Entry page: [http://localhost:9302/update-and-submit-income-tax-return/2025/start](http://localhost:9302/update-and-submit-income-tax-return/2025/start)

| Enrolment Key | Identifier Name | Identifier Value |
|---------------|-----------------|------------------|
| HMRC-MTD-IT   | MTDITID         | 1234567890       |


### Agent
* Login via: [http://localhost:9949/auth-login-stub/gg-sign-in](http://localhost:9949/auth-login-stub/gg-sign-in)
* Entry page : [http://localhost:9302/update-and-submit-income-tax-return/test-only/2024/additional-parameters?ClientNino=AC160000B&ClientMTDID=1234567890](http://localhost:9302/update-and-submit-income-tax-return/test-only/2024/additional-parameters?ClientNino=AC160000B&ClientMTDID=1234567890)

| Enrolment Key  | Identifier Name      | Identifier Value	 |
|----------------|----------------------|-------------------|
| HMRC-MTD-IT    | MTDITID              | 1234567890        |
| HMRC-AS-AGENT  | AgentReferenceNumber | XARN1234567       |

## Staging

*Requires HMRC VPN*

### Individual
* Login via: [https://www.staging.tax.service.gov.uk/auth-login-stub/gg-sign-in](https://www.staging.tax.service.gov.uk/auth-login-stub/gg-sign-in)
* Entry page : [https://www.staging.tax.service.gov.uk/update-and-submit-income-tax-return/2026/start](https://www.staging.tax.service.gov.uk/update-and-submit-income-tax-return/2026/start)

| Enrolment Key | Identifier Name | Identifier Value |
|---------------|-----------------|------------------|
| HMRC-MTD-IT   | MTDITID         | 1234567890       |

### Agent
* Login via: [https://www.staging.tax.service.gov.uk/auth-login-stub/gg-sign-in](https://www.staging.tax.service.gov.uk/auth-login-stub/gg-sign-in)
* Entry page : [https://www.staging.tax.service.gov.uk/update-and-submit-income-tax-return/test-only/2026/additional-parameters?ClientNino=AC160000B&ClientMTDID=1234567890](https://www.staging.tax.service.gov.uk/update-and-submit-income-tax-return/test-only/2026/additional-parameters?ClientNino=AC160000B&ClientMTDID=1234567890)

| Enrolment Key  | Identifier Name      | Identifier Value	 |
|----------------|----------------------|-------------------|
| HMRC-MTD-IT    | MTDITID              | 1234567890        |
| HMRC-AS-AGENT  | AgentReferenceNumber | XARN1234567       |


### License

This code is open source software licensed under the [Apache 2.0 License]("http://www.apache.org/licenses/LICENSE-2.0.html").