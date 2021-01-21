public class Staircases {
    public static void main(String[] args) {
        System.out.println(solution(200));
    }
    public static int solution(int n) {
        return countSteps(n, n + 1);
    }
    // Recursively checks for and counts all valid staircases.
    // Leverages the 1+2+3...+n series to avoid an exhaustive search.
    private static int countSteps(int n, int next) {
        int counter = 0;
        for (int i = n - 1; n <= (i * (i + 1) / 2); i--) {
            if (i >= next) {
                continue;
            }
            if (i > n - i) {
                counter++;
            }
            if (n - i > 2) {
                counter += countSteps(n - i, i);
            }
        }
        return counter;
    }
}