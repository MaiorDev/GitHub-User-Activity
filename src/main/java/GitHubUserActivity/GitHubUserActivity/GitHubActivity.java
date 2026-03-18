package GitHubUserActivity.GitHubUserActivity;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Scanner;

public class GitHubActivity {
    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        System.out.print("Enter GitHub username: ");
        String username = scanner.nextLine();

        String url = "https://api.github.com/users/" + username + "/events";
        HttpClient client = HttpClient.newHttpClient();
        HttpRequest request = HttpRequest.newBuilder().uri(URI.create(url)).build();

        try {
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() == 404) {
                System.out.println("Error: User not found.");
                return;
            }

            parseAndDisplay(response.body());

        } catch (Exception e) {
            System.out.println("Error: " + e.getMessage());
        }
    }


    private static void parseAndDisplay(String json) {
        System.out.println("Output:");

        String[] events = json.split("\\{\"id\":\"");

        for (int i = 1; i < Math.min(events.length, 10); i++) {
            String event = events[i];

            // 2. Use Regex to find the "type" and the "repo name"
            String type = findMatch(event, "\"type\":\"(.*?)\"");
            String repoName = findMatch(event, "\"name\":\"(.*?)\"");

            if (type == null || repoName == null) continue;

            // 3. Special logic for PushEvents to count commits
            if (type.equals("PushEvent")) {
                String commitCount = countCommits(event);
                System.out.println("- Pushed " + commitCount + " commit(s) to " + repoName);
            } else {
                System.out.println("- " + formatAction(type) + " in " + repoName);
            }
        }
    }

    private static String findMatch(String text, String regex) {
        java.util.regex.Pattern pattern = java.util.regex.Pattern.compile(regex);
        java.util.regex.Matcher matcher = pattern.matcher(text);
        return matcher.find() ? matcher.group(1) : null;
    }

    private static String countCommits(String event) {
        String size = findMatch(event, "\"size\":(\\d+)");
        return (size != null) ? size : "1";
    }

    private static String formatAction(String type) {
        switch (type) {
            case "IssuesEvent": return "Opened a new issue";
            case "WatchEvent":  return "Starred";
            case "CreateEvent": return "Created a repository";
            case "IssueCommentEvent": return "Commented on an issue";
            default: return type.replace("Event", "");
        }
    }
}