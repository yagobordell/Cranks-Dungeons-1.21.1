/*
 * Decompiled with CFR 0.152.
 */
package punchy.client.animation.data;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public final class AnimationExpression {
    private final Node root;

    private AnimationExpression(Node root) {
        this.root = root;
    }

    public static AnimationExpression compile(String source) {
        if (source == null) {
            return null;
        }
        String trimmed = source.trim();
        if (trimmed.isEmpty()) {
            return null;
        }
        try {
            Parser parser = new Parser(trimmed);
            Node node = parser.parse();
            if (node == null) {
                return null;
            }
            return new AnimationExpression(node);
        }
        catch (Exception ignored) {
            return null;
        }
    }

    public double evaluate(double animTimeSeconds) {
        return this.root.eval(animTimeSeconds);
    }

    public static AnimationExpression negated(AnimationExpression base) {
        if (base == null) {
            return null;
        }
        return new AnimationExpression(animTimeSeconds -> -base.evaluate(animTimeSeconds));
    }

    private static interface Node {
        public double eval(double var1);
    }

    private static final class Parser {
        private final String src;
        private int pos;

        private Parser(String src) {
            this.src = src;
        }

        private Node parse() {
            Node n = this.parseExpression();
            this.skipWs();
            return this.pos == this.src.length() ? n : null;
        }

        private Node parseExpression() {
            Node left = this.parseTerm();
            while (true) {
                this.skipWs();
                if (this.match('+')) {
                    left = new BinaryNode('+', left, this.parseTerm());
                    continue;
                }
                if (!this.match('-')) break;
                left = new BinaryNode('-', left, this.parseTerm());
            }
            return left;
        }

        private Node parseTerm() {
            Node left = this.parseUnary();
            while (true) {
                this.skipWs();
                if (this.match('*')) {
                    left = new BinaryNode('*', left, this.parseUnary());
                    continue;
                }
                if (!this.match('/')) break;
                left = new BinaryNode('/', left, this.parseUnary());
            }
            return left;
        }

        private Node parseUnary() {
            this.skipWs();
            if (this.match('+')) {
                return new UnaryNode('+', this.parseUnary());
            }
            if (this.match('-')) {
                return new UnaryNode('-', this.parseUnary());
            }
            return this.parsePrimary();
        }

        private Node parsePrimary() {
            this.skipWs();
            if (this.match('(')) {
                Node inner = this.parseExpression();
                this.expect(')');
                return inner == null ? new ConstantNode(0.0) : inner;
            }
            if (this.peekNumberStart()) {
                return new ConstantNode(this.parseNumber());
            }
            String id = this.parseIdentifier();
            if (id == null || id.isEmpty()) {
                return new ConstantNode(0.0);
            }
            String normalized = this.normalizeIdentifier(id);
            this.skipWs();
            if (this.match('(')) {
                ArrayList<Node> args = new ArrayList<Node>();
                this.skipWs();
                if (!this.peek(')')) {
                    do {
                        args.add(this.parseExpression());
                        this.skipWs();
                    } while (this.match(','));
                }
                this.expect(')');
                return new FunctionNode(normalized, args);
            }
            if ("query.anim_time".equals(normalized) || "query.animtime".equals(normalized)) {
                return VariableNode.ANIM_TIME;
            }
            if ("pi".equals(normalized) || "math.pi".equals(normalized)) {
                return new ConstantNode(Math.PI);
            }
            if ("e".equals(normalized) || "math.e".equals(normalized)) {
                return new ConstantNode(Math.E);
            }
            return new ConstantNode(0.0);
        }

        private String normalizeIdentifier(String id) {
            String fn;
            String out = id.trim().toLowerCase(Locale.ROOT);
            if (out.startsWith("math.") && !(fn = out.substring("math.".length())).isBlank()) {
                return fn;
            }
            return out;
        }

        private boolean peekNumberStart() {
            if (this.pos >= this.src.length()) {
                return false;
            }
            char c = this.src.charAt(this.pos);
            return Character.isDigit(c) || c == '.';
        }

        private double parseNumber() {
            int start = this.pos;
            while (this.pos < this.src.length()) {
                char c = this.src.charAt(this.pos);
                if (Character.isDigit(c) || c == '.') {
                    ++this.pos;
                    continue;
                }
                if (c != 'e' && c != 'E' || this.pos + 1 >= this.src.length()) break;
                ++this.pos;
                if (this.src.charAt(this.pos) != '+' && this.src.charAt(this.pos) != '-') continue;
                ++this.pos;
            }
            try {
                return Double.parseDouble(this.src.substring(start, this.pos));
            }
            catch (Exception ignored) {
                return 0.0;
            }
        }

        private String parseIdentifier() {
            char c;
            this.skipWs();
            if (this.pos >= this.src.length()) {
                return null;
            }
            int start = this.pos;
            char first = this.src.charAt(this.pos);
            if (!Character.isLetter(first) && first != '_') {
                return null;
            }
            ++this.pos;
            while (this.pos < this.src.length() && (Character.isLetterOrDigit(c = this.src.charAt(this.pos)) || c == '_' || c == '.')) {
                ++this.pos;
            }
            return this.src.substring(start, this.pos);
        }

        private void skipWs() {
            while (this.pos < this.src.length() && Character.isWhitespace(this.src.charAt(this.pos))) {
                ++this.pos;
            }
        }

        private boolean match(char c) {
            if (this.peek(c)) {
                ++this.pos;
                return true;
            }
            return false;
        }

        private boolean peek(char c) {
            this.skipWs();
            return this.pos < this.src.length() && this.src.charAt(this.pos) == c;
        }

        private void expect(char c) {
            this.skipWs();
            if (this.pos < this.src.length() && this.src.charAt(this.pos) == c) {
                ++this.pos;
            }
        }
    }

    private record FunctionNode(String name, List<Node> args) implements Node
    {
        @Override
        public double eval(double animTimeSeconds) {
            ArrayList<Double> values = new ArrayList<Double>(this.args.size());
            for (Node arg : this.args) {
                values.add(arg.eval(animTimeSeconds));
            }
            return switch (this.name) {
                case "sin" -> {
                    if (values.isEmpty()) {
                        yield 0.0;
                    }
                    yield Math.sin(Math.toRadians((Double)values.get(0)));
                }
                case "cos" -> {
                    if (values.isEmpty()) {
                        yield 0.0;
                    }
                    yield Math.cos(Math.toRadians((Double)values.get(0)));
                }
                case "tan" -> {
                    if (values.isEmpty()) {
                        yield 0.0;
                    }
                    yield Math.tan(Math.toRadians((Double)values.get(0)));
                }
                case "abs" -> {
                    if (values.isEmpty()) {
                        yield 0.0;
                    }
                    yield Math.abs((Double)values.get(0));
                }
                case "sqrt" -> {
                    if (values.isEmpty()) {
                        yield 0.0;
                    }
                    yield Math.sqrt(Math.max(0.0, (Double)values.get(0)));
                }
                case "floor" -> {
                    if (values.isEmpty()) {
                        yield 0.0;
                    }
                    yield Math.floor((Double)values.get(0));
                }
                case "ceil" -> {
                    if (values.isEmpty()) {
                        yield 0.0;
                    }
                    yield Math.ceil((Double)values.get(0));
                }
                case "round" -> {
                    if (values.isEmpty()) {
                        yield 0.0;
                    }
                    yield Math.rint((Double)values.get(0));
                }
                case "min" -> {
                    if (values.isEmpty()) {
                        yield 0.0;
                    }
                    double out = (Double)values.get(0);
                    for (int i = 1; i < values.size(); ++i) {
                        out = Math.min(out, (Double)values.get(i));
                    }
                    yield out;
                }
                case "max" -> {
                    if (values.isEmpty()) {
                        yield 0.0;
                    }
                    double out = (Double)values.get(0);
                    for (int i = 1; i < values.size(); ++i) {
                        out = Math.max(out, (Double)values.get(i));
                    }
                    yield out;
                }
                case "clamp" -> {
                    if (values.size() < 3) {
                        if (values.isEmpty()) {
                            yield 0.0;
                        }
                        yield (Double)values.get(0);
                    }
                    yield Math.max((Double)values.get(1), Math.min((Double)values.get(2), (Double)values.get(0)));
                }
                default -> 0.0;
            };
        }
    }

    private record BinaryNode(char op, Node left, Node right) implements Node
    {
        @Override
        public double eval(double animTimeSeconds) {
            double a = this.left.eval(animTimeSeconds);
            double b = this.right.eval(animTimeSeconds);
            return switch (this.op) {
                case '+' -> a + b;
                case '-' -> a - b;
                case '*' -> a * b;
                case '/' -> {
                    if (b == 0.0) {
                        yield 0.0;
                    }
                    yield a / b;
                }
                default -> 0.0;
            };
        }
    }

    private record UnaryNode(char op, Node operand) implements Node
    {
        @Override
        public double eval(double animTimeSeconds) {
            double v = this.operand.eval(animTimeSeconds);
            return this.op == '-' ? -v : v;
        }
    }

    private static enum VariableNode implements Node
    {
        ANIM_TIME;


        @Override
        public double eval(double animTimeSeconds) {
            return animTimeSeconds;
        }
    }

    private record ConstantNode(double value) implements Node
    {
        @Override
        public double eval(double animTimeSeconds) {
            return this.value;
        }
    }
}

