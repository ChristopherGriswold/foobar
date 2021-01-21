public class DoomsdayFuel {
    public static int standardBorder;
    public static int denominator;

    // Uses gaussian elimination to manipulate a stochastic matrix in
    // order to find it's limiting values. The matrix is first put into standard
    // form and its state transition frequencies are converted into relative probabilities.
    // Q and R sub-matrices are identified, extracted and operated on in order to
    // build the limiting matrix denoted as Pbar. All values are stored as integers
    // with a common denominator. LCM Functions utilize long values during computation just to be safe.
    public static int[] solution(int[][] m) {
        standardBorder = 0;
        denominator = 0;
        m = standardize(m);
        transformMatrix(m);
        int[][] Qmatrix = getQMatrix(m, standardBorder);
        int[][] Fmatrix = getFMatrix(Qmatrix, denominator);
        int[][] Rmatrix = getRMatrix(m, standardBorder);
        int[][] fr = multiplyRectMatrix(Fmatrix, Rmatrix);
        int[][] pbar = getPbar(m, fr);
        return extractAnswer(pbar);
    }

    // The final step. It pulls the the pertinent values from Pbar
    // and formats them for output.
    private static int[] extractAnswer(int[][] matrix) {
        int[] temp = new int[matrix[0].length];
        for (int i = 1; i < matrix.length; i++) {
            if (matrix[i][0] == 0) {
                temp = matrix[i];
                break;
            }
        }
        int numTerminals = standardBorder;
        int[] out = new int[numTerminals + 1];
        for (int i = 0; i < out.length - 1; i++) {
            out[i] = temp[i + 1];
        }
        easySimple(out);
        out[out.length - 1] = sumArrayAbs(out);

        return out;
    }

    // Puts the transition matrix in standard form and adds a header
    // row and column to preserve the ability to identify individual elements.
    private static int[][] standardize(int[][] matrix) {
        int rows = matrix.length + 1;
        int cols = matrix[0].length + 1;
        int[][] standard = new int[rows][cols];
        standard[0][0] = -1;
        // Add headers
        for (int i = 1; i < rows; i++) {
            standard[0][i] = i - 1;
            standard[i][0] = i - 1;
        }
        for (int i = 0; i < rows - 1; i++) {
            for (int j = 0; j < cols - 1; j++) {
                standard[i + 1][j + 1] = matrix[i][j];
            }
        }
        // Add 1s to terminals. A border value is used to keep track
        // of the partitions pertaining to the Q and R sub-matrices.
        int border = 0;
        for (int i = 1; i < rows; i++) {
            boolean isTerminal = true;
            for (int j = 1; j < cols; j++) {
                if (standard[i][j] != 0 && i != j) {
                    isTerminal = false;
                    break;
                }
            }
            if (isTerminal) {
                // Swap column
                int colIndex = standard[i][0];
                for (int j = 0; j < rows; j++) {
                    int temp = standard[j][colIndex + 1];
                    standard[j][colIndex + 1] = standard[j][border + 1];
                    standard[j][border + 1] = temp;
                }
                int[] temp = standard[border + 1];
                standard[border + 1] = standard[i];
                standard[i] = temp;
                standard[border + 1][border + 1] = 1;
                border++;
            }
        }
        standardBorder = border;
        return standard;
    }

    private static int[][] getFMatrix(int[][] qMatrix, int scale) {
        int[][] identity = getIdentity(qMatrix.length, scale);
        int[][] sub = subtractMatrix(identity, qMatrix);
        int[][] inverted = invertMatrix(sub);
        return inverted;
    }

    private static int[][] getQMatrix(int[][] matrix, int border) {
        int[][] outQ = new int[matrix.length - (border + 1)][matrix.length - (border + 1)];
        for (int i = 0; i < outQ.length; i++) {
            for (int j = 0; j < outQ[0].length; j++) {
                outQ[i][j] = matrix[border + 1 + i][border + 1 + j];
                matrix[border + 1 + i][border + 1 + j] = 0;
            }
        }
        return outQ;
    }

    private static int[][] getRMatrix(int[][] matrix, int border) {
        int[][] outR = new int[matrix.length - (border + 1)][border];
        for (int i = (border + 1); i < matrix.length; i++) {
            for (int j = 1; j < (border + 1); j++) {
                outR[i - (border + 1)][j - 1] = matrix[i][j];
                matrix[i][j] = 0;
            }
        }
        return outR;
    }

    private static int[][] getPbar(int[][] mainMatrix, int[][] fr) {
        int[][] out = mainMatrix.clone();
        for (int i = 0; i < fr.length; i++) {
            for (int j = 0; j < fr[0].length; j++) {
                out[mainMatrix.length - 1 - i][j + 1] = fr[fr.length - 1 - i][j];
            }
        }
        for (int i = mainMatrix.length - 1; i >= mainMatrix.length - fr.length; i--) {
            for (int j = fr[0].length + 1; j < mainMatrix[0].length; j++) {
                out[i][j] = 0;
            }
        }
        return out;
    }

    private static int[][] invertMatrix(int[][] matrix) {
        if (matrix.length < 1) {
            return new int[0][0];
        }
        int rows = matrix.length;
        int cols = matrix[0].length * 2;
        int[][] augMatrix = new int[rows][cols];
        for (int i = 0; i < matrix.length; i++) {
            for (int j = 0; j < matrix[0].length; j++) {
                augMatrix[i][j] = matrix[i][j];
            }
        }
        // Augment with identity matrix
        for (int i = 0, j = matrix[0].length; i < augMatrix.length; i++, j++) {
            augMatrix[i][j] = denominator;
        }
        for (int i = 0; i < (cols - (cols / 2)) - 1; i++) {
            orderAugMatrix(augMatrix,  i);
            for (int j = i; j < rows; j++) {
                if (augMatrix[j][i] < 0) {
                    flipSigns(augMatrix[j]);
                }
            }
            if (i == cols - (cols / 2) - 1) {
                continue;
            }
            for (int j = i + 1; j < rows; j++) {
                rowOperation(augMatrix, j, i);
            }
        }

        // Zero top right
        int rightSide = (cols / 2) - 1;
        for (int i = rightSide; i >= 0; i--) {
            for (int j = rows - (rightSide - i) - 2; j >= 0; j--) {
                rowOperation(augMatrix, j, i);
            }
        }
        long lcm = 1;
        for (int i = 0; i < rows; i++) {
            if (augMatrix[i][i] == getArrayGcd(augMatrix[i])) {
                long divisor = augMatrix[i][i];
                for (int j = i; j < cols; j++) {
                    augMatrix[i][j] /= divisor;
                }
            } else {
                lcm = getLcm(lcm, augMatrix[i][i]);
                long divisor = lcm / augMatrix[i][i];
                for (int j = i; j < cols; j++) {
                    augMatrix[i][j] *= divisor;
                }
            }
            // Be sure to simplify as we go to avoid overflow.
            easySimple(augMatrix[i]);
        }
        // Strip off the identity side of the augmented matrix.
        int[][] out = new int[rows][cols / 2];
        for (int i = 0; i < rows; i++) {
            for (int j = cols / 2; j < cols; j++) {
                out[i][j - (cols / 2)] = augMatrix[i][j];
            }
            easySimple(augMatrix[i]);
        }
        return out;
    }

    private static void rowOperation(int[][] augMatrix, int t, int col) {
        int b = col;
        int[] tempBRow = augMatrix[b].clone();
        long lcm = getLcm(Math.abs(augMatrix[b][col]), Math.abs(augMatrix[t][col]));
        if (lcm == 0) {
            return;
        }
        lcm = (lcm == 0) ? 1 : lcm;
        long originFactor = (augMatrix[b][col] == 0) ? 0 : (lcm / augMatrix[b][col]);
        long targetFactor = (augMatrix[t][col] == 0) ? 0 : (lcm / augMatrix[t][col]);

        for (int i = 0; i < augMatrix[t].length; i++) {
            tempBRow[i] = (int)(augMatrix[b][i] * originFactor);
            augMatrix[t][i] *= targetFactor;
            augMatrix[t][i] -= tempBRow[i];
        }
        easySimple(augMatrix[t]);
    }

    private static void orderAugMatrix(int[][] augMatrix, int position) {
        for (int i = position; i < augMatrix.length - 1; i++) {
            for (int j = i + 1; j < augMatrix.length; j++) {
                if (Math.abs(augMatrix[j][position]) > Math.abs(augMatrix[i][position])) {
                    int[] temp = augMatrix[i];
                    augMatrix[i] = augMatrix[j];
                    augMatrix[j] = temp;
                }
            }
        }
    }

    private static void flipSigns(int[] arr) {
        for (int i = 0; i < arr.length; i++) {
            arr[i] *= -1;
        }
    }

    private static int[][] getIdentity(int size, int scale) {
        int[][] out = new int[size][size];
        for (int i = 0; i < size; i++) {
            out[i][i] = scale;
        }
        return out;
    }

    private static void transformMatrix(int[][] m) {
        int[][] temp = new int[m.length - 1][m[0].length - 1];
        for (int i = 0; i < temp.length; i++) {
            for (int j = 0; j < temp[0].length; j++) {
                temp[i][j] = m[i + 1][j + 1];
            }
        }
        denominator = (int) getDenominator(temp);
        for (int i = 0; i < temp.length; i++) {
            long sum = sumArrayAbs(temp[i]);
            for(int j = 0; j < temp[i].length; j++) {
                if(m[i + 1][j + 1] == 0) {
                    m[i + 1][j + 1] = 0;
                } else {
                    m[i + 1][j + 1] = (int)((denominator / sum) * m[i + 1][j + 1]);
                }
            }
        }
    }

    private static int[][] multiplyRectMatrix(int[][] m1, int[][] m2) {
        if (m1.length < 1 || m2.length < 1) {
            return new int[0][0];
        }
        if (m1[0].length != m2.length) {
            int[][] temp = m1;
            m1 = m2;
            m2 = temp;
        }

        int[][] out = new int[m2.length][m2[0].length];
        for(int i = 0; i < m2.length; i++) {
            for (int j = 0; j < m2[0].length; j++) {
                for (int k = 0; k < m1[0].length; k++) {
                    out[i][j] += m1[i][k] * m2[k][j];
                }
            }
        }
        return out;
    }

    private static int[][] subtractMatrix(int[][] a, int[][] b) {
        if (a.length < 1 || b.length < 1) {
            return new int[0][0];
        }
        int[][] out = new int[a.length][a[0].length];
        for (int i = 0; i < a.length; i++) {
            for (int j = 0; j < a[i].length; j++) {
                out[i][j] = a[i][j] - b[i][j];
            }
        }
        return out;
    }

    private static long getDenominator(int[][] m) {
        long[] sum = new long[m.length];
        for (int i = 0; i < m.length; i++) {
            for (int j = 0; j < m[i].length; j++) {
                sum[i] += Math.abs(m[i][j]);
            }
        }
        return getArrayLcm(sum);
    }

    private static long getArrayLcm(long[] arr) {
        long lcm = 0;
        for (int i = 0; i < arr.length; i++) {
            if (arr[i] == 0) {
                continue;
            }
            if (lcm == 0) {
                lcm = Math.abs(arr[i]);
            } else {
                lcm = getLcm(lcm, Math.abs(arr[i]));
            }
        }
        return lcm;
    }

    private static long getLcm(long a, long b) {
        if (a == 0 || b == 0) {
            return 0;
        }
        long tempA = Math.abs(a);
        long tempB = Math.abs(b);
        long high = Math.max(tempA, tempB);
        long low = Math.min(tempA, tempB);
        long lcm = high;
        while (lcm % low != 0) {
            lcm += high;
        }
        return lcm;
    }

    private static int getGcd(int n1, int n2) {
        n1 = Math.abs(n1);
        n2 = Math.abs(n2);
        if (n2 == 0) {
            return n1;
        }
        return getGcd(n2, n1 % n2);
    }

    private static int sumArrayAbs(int[] arr) {
        int sum = 0;
        for (int i = 0; i < arr.length; i++) {
            sum += Math.abs(arr[i]);
        }
        return sum;
    }

    private static void easySimple(int[] arr) {
        int gcd = getArrayGcd(arr);
        for (int i = 0; i < arr.length; i++) {
            arr[i] /= gcd;
        }
    }

    private static int getArrayGcd(int[] arr) {
        int rows = arr.length;
        int gcd = 1;
        for (int i = 0; i < rows; i++) {
            if (Math.abs(arr[i]) > 0) {
                gcd = Math.abs(arr[i]);
                break;
            }
        }
        for (int i = 0; i < rows; i++) {
            if (Math.abs(arr[i]) > 0) {
                gcd = getGcd(gcd, Math.abs(arr[i]));
            }
        }
        return gcd;
    }
}