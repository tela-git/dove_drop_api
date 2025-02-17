package com.example.routes

import io.ktor.http.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import kotlinx.serialization.Serializable

fun Route.agreementRoutes() {
    get("/ag/tAndC") {
        val termsAndConditions = TermsAndConditions(tAndCText)
        call.respond(HttpStatusCode.OK, termsAndConditions)
    }
}

@Serializable
data class TermsAndConditions(
    val termsAndConditions: String = ""
)

private const val tAndCText = "**Terms and Conditions**  \n" +
        "**Last Updated: 15 February 2025**  \n" +
        "\n" +
        "Welcome to DOVE DROP (\"Application\"). By using our services, you agree to abide by these Terms and Conditions. Please read them carefully before accessing or using the Application.\n" +
        "\n" +
        "---\n" +
        "\n" +
        "### 1. Acceptance of Terms  \n" +
        "By accessing or using the Application, you agree to these Terms and Conditions, as well as our Privacy Policy. If you do not agree with any part of these terms, please do not use the Application.\n" +
        "\n" +
        "### 2. User Eligibility  \n" +
        "You must be at least 13 years old (or the minimum age required by local law) to use the Application. If you are under the age of 18, you confirm that you have obtained parental or guardian consent before using the Application.\n" +
        "\n" +
        "### 3. User Conduct  \n" +
        "When using the Application, you agree to:\n" +
        "- Not engage in harassment, hate speech, or any form of abusive behavior.\n" +
        "- Not share, send, or store illegal, harmful, or inappropriate content.\n" +
        "- Not impersonate another person or misrepresent your identity.\n" +
        "- Not attempt to hack, disrupt, or interfere with the Application’s functionality.\n" +
        "\n" +
        "### 4. Content Ownership & Usage  \n" +
        "- You retain ownership of any content you submit, post, or share through the Application.\n" +
        "- By using the Application, you grant us a limited license to store, process, and display your content as necessary for providing the service.\n" +
        "- We do not claim ownership of your private messages or conversations.\n" +
        "\n" +
        "### 5. Privacy & Data Protection  \n" +
        "Your privacy is important to us. Our Privacy Policy explains how we collect, use, and store your data. By using the Application, you acknowledge and agree to our data practices.\n" +
        "\n" +
        "### 6. Account Security  \n" +
        "- You are responsible for maintaining the confidentiality of your login credentials.\n" +
        "- We are not responsible for any unauthorized access to your account.\n" +
        "- If you suspect any security breaches, please contact us immediately.\n" +
        "\n" +
        "### 7. Termination  \n" +
        "We reserve the right to suspend or terminate your access to the Application if you violate these Terms and Conditions. We may also terminate accounts that remain inactive for an extended period.\n" +
        "\n" +
        "### 8. Limitation of Liability  \n" +
        "- The Application is provided \"as is\" without warranties of any kind.\n" +
        "- We are not responsible for any damages, losses, or liabilities resulting from your use of the Application.\n" +
        "- We do not guarantee uninterrupted or error-free service.\n" +
        "\n" +
        "### 9. Changes to Terms  \n" +
        "We may update these Terms and Conditions from time to time. Any changes will be posted within the Application, and continued use constitutes acceptance of the updated terms.\n" +
        "\n" +
        "### 10. Contact Information  \n" +
        "For any questions regarding these Terms and Conditions, please contact us at [Your Contact Information].\n" +
        "\n" +
        "---\n" +
        "By using DOVE DROP, you agree to these Terms and Conditions. If you do not accept these terms, please discontinue use immediately.\n" +
        "\n"