/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.kylin.query.util;

import org.apache.calcite.avatica.util.Quoting;
import org.apache.calcite.sql.parser.SqlParser;

import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.ArrayList;
import java.util.EnumSet;

/**
 * copy from the version of 1.26.0 {@link org.apache.calcite.sql.advise.SqlSimpleParser}
 */
public class SqlSimpleParser {
    //~ Enums ------------------------------------------------------------------

    /**
     * Token.
     */
    enum TokenType {
        // keywords
        SELECT, FROM, JOIN, ON, USING, WHERE, GROUP, HAVING, ORDER, BY,

        UNION, INTERSECT, EXCEPT, MINUS,

        /**
         * Left parenthesis.
         */
        LPAREN {
            public String sql() {
                return "(";
            }
        },

        /**
         * Right parenthesis.
         */
        RPAREN {
            public String sql() {
                return ")";
            }
        },

        /**
         * Identifier, or indeed any miscellaneous sequence of characters.
         */
        ID,

        /**
         * double-quoted identifier, e.g. "FOO""BAR"
         */
        DQID,

        /**
         * single-quoted string literal, e.g. 'foobar'
         */
        SQID, COMMENT,
        COMMA {
            public String sql() {
                return ",";
            }
        },

        /**
         * A token created by reducing an entire sub-query.
         */
        QUERY;

        public String sql() {
            return name();
        }
    }

    //~ Instance fields --------------------------------------------------------

    private final String hintToken;
    private final SqlParser.Config parserConfig;

    //~ Constructors -----------------------------------------------------------

    /**
     * Creates a SqlSimpleParser.
     *
     * @param hintToken Hint token
     * @deprecated Use {@link #SqlSimpleParser(String, SqlParser.Config)}
     */
    @Deprecated // to be removed before 2.0
    public SqlSimpleParser(String hintToken) {
        this(hintToken, SqlParser.Config.DEFAULT);
    }

    /**
     * Creates a SqlSimpleParser.
     *
     * @param hintToken    Hint token
     * @param parserConfig parser configuration
     */
    public SqlSimpleParser(String hintToken,
                           SqlParser.Config parserConfig) {
        this.hintToken = hintToken;
        this.parserConfig = parserConfig;
    }

    //~ Methods ----------------------------------------------------------------

    public String removeCommentSql(String sql) {
        Tokenizer tokenizer = new Tokenizer(sql, hintToken, parserConfig.quoting());
        StringBuilder newSQL = new StringBuilder();
        int startIndex = 0;
        while (true) {
            Token token = tokenizer.nextToken();
            if (token == null) {
                if (startIndex != sql.length()) {
                    newSQL.append(sql, startIndex, sql.length());
                }
                break;
            }
            if (token.type == TokenType.COMMENT) {
                // ignore comments
                newSQL.append(sql, startIndex, token.startIndex);
                startIndex = token.endIndex;
            }
        }
        return newSQL.toString();

    }

    //~ Inner Classes ----------------------------------------------------------

    /**
     * Tokenizer.
     */
    public static class Tokenizer {
        private static final Map<String, TokenType> TOKEN_TYPES = new HashMap<>();

        static {
            for (TokenType type : TokenType.values()) {
                TOKEN_TYPES.put(type.name(), type);
            }
        }

        final String sql;
        private final String hintToken;
        private final char openQuote;
        private int pos;
        int start = 0;

        @Deprecated // to be removed before 2.0
        public Tokenizer(String sql, String hintToken) {
            this(sql, hintToken, Quoting.DOUBLE_QUOTE);
        }

        public Tokenizer(String sql, String hintToken, Quoting quoting) {
            this.sql = sql;
            this.hintToken = hintToken;
            this.openQuote = quoting.string.charAt(0);
            this.pos = 0;
        }

        private Token parseQuotedIdentifier() {
            // Parse double-quoted identifier.
            start = pos;
            ++pos;
            char closeQuote = openQuote == '[' ? ']' : openQuote;
            while (pos < sql.length()) {
                char c = sql.charAt(pos);
                ++pos;
                if (c == closeQuote) {
                    if (pos < sql.length() && sql.charAt(pos) == closeQuote) {
                        // Double close means escaped closing quote is a part of identifer
                        ++pos;
                        continue;
                    }
                    break;
                }
            }
            String match = sql.substring(start, pos);
            if (match.startsWith(openQuote + " " + hintToken + " ")) {
                return new Token(TokenType.ID, hintToken);
            }
            return new Token(TokenType.DQID, match);
        }

        public Token nextToken() {
            while (pos < sql.length()) {
                char c = sql.charAt(pos);
                final String match;
                Integer startIndex = null;
                switch (c) {
                    case ',':
                        startIndex = pos;
                        ++pos;
                        return new Token(TokenType.COMMA, startIndex, pos);

                    case '(':
                        startIndex = pos;
                        ++pos;
                        return new Token(TokenType.LPAREN, startIndex, pos);

                    case ')':
                        startIndex = pos;
                        ++pos;
                        return new Token(TokenType.RPAREN, startIndex, pos);

                    case '\'':
                        startIndex = pos;
                        // Parse single-quoted identifier.
                        start = pos;
                        ++pos;
                        while (pos < sql.length()) {
                            c = sql.charAt(pos);
                            ++pos;
                            if (c == '\'') {
                                if (pos < sql.length()) {
                                    char c1 = sql.charAt(pos);
                                    if (c1 == '\'') {
                                        // encountered consecutive
                                        // single-quotes; still in identifier
                                        ++pos;
                                    } else {
                                        break;
                                    }
                                } else {
                                    break;
                                }
                            }
                        }
                        match = sql.substring(start, pos);
                        return new Token(TokenType.SQID, match, startIndex, pos);

                    case '/':

                        // possible start of '/*' or '//' comment
                        if (pos + 1 < sql.length()) {
                            char c1 = sql.charAt(pos + 1);
                            if (c1 == '*') {
                                startIndex = pos;
                                int end = sql.indexOf("*/", pos + 2);
                                if (end < 0) {
                                    end = sql.length();
                                } else {
                                    end += "*/".length();
                                }
                                pos = end;
                                Integer endIndex = pos;
                                return new Token(TokenType.COMMENT, startIndex, endIndex);
                            }
                            if (c1 == '/') {
                                startIndex = pos;
                                pos = indexOfLineEnd(sql, pos + 2);
                                Integer endIndex = pos;
                                return new Token(TokenType.COMMENT, startIndex, endIndex);
                            }
                        }
                        // fall through

                    case '-':
                        // possible start of '--' comment
                        if (c == '-' && pos + 1 < sql.length() && sql.charAt(pos + 1) == '-') {
                            startIndex = pos;
                            pos = indexOfLineEnd(sql, pos + 2);
                            Integer endIndex = pos;
                            return new Token(TokenType.COMMENT, startIndex, endIndex);
                        }
                        // fall through


                    default:
                        if (c == openQuote) {
                            return parseQuotedIdentifier();
                        }
                        if (Character.isWhitespace(c)) {
                            ++pos;
                            break;
                        } else {
                            // Probably a letter or digit. Start an identifier.
                            // Other characters, e.g. *, ! are also included
                            // in identifiers.
                            int start = pos;
                            ++pos;
                            loop:
                            while (pos < sql.length()) {
                                c = sql.charAt(pos);
                                switch (c) {
                                    case '(':
                                    case ')':
                                    case '/':
                                    case ',':
                                        break loop;
                                    case '-':
                                        // possible start of '--' comment
                                        if (c == '-' && pos + 1 < sql.length() && sql.charAt(pos + 1) == '-') {
                                            startIndex = pos;
                                            pos = indexOfLineEnd(sql, pos + 2);
                                            Integer endIndex = pos;
                                            return new Token(TokenType.COMMENT, startIndex, endIndex);
                                        }
                                    default:
                                        if (Character.isWhitespace(c)) {
                                            break loop;
                                        } else {
                                            ++pos;
                                        }
                                }
                            }
                            String name = sql.substring(start, pos);
                            TokenType tokenType = TOKEN_TYPES.get(name.toUpperCase(Locale.ROOT));
                            if (tokenType == null) {
                                return new IdToken(TokenType.ID, name, start, pos);
                            } else {
                                // keyword, e.g. SELECT, FROM, WHERE
                                Token token = new Token(tokenType);
                                token.setOriginTokenType(name);
                                token.startIndex = start;
                                token.endIndex = pos;
                                return token;
                            }
                        }
                }
            }
            return null;
        }

        private int indexOfLineEnd(String sql, int i) {
            int length = sql.length();
            while (i < length) {
                char c = sql.charAt(i);
                switch (c) {
                    case '\r':
                    case '\n':
                        return i;
                    default:
                        ++i;
                }
            }
            return i;
        }
    }

    /**
     * Token.
     */
    public static class Token {


        private final TokenType type;
        private final String s;
        private Integer startIndex;
        private Integer endIndex;

        public Integer getStartIndex() {
            return startIndex;
        }

        public Integer getEndIndex() {
            return endIndex;
        }

        private String originTokenType;

        public String getOriginTokenType() {
            return originTokenType;
        }

        public void setOriginTokenType(String originTokenType) {
            this.originTokenType = originTokenType;
        }

        Token(TokenType tokenType) {
            this(tokenType, null, null, null);
        }

        Token(TokenType tokenType, String s) {
            this(tokenType, s, null, null);
        }

        Token(TokenType type, String s, Integer startIndex, Integer endIndex) {
            this.type = type;
            this.s = s;
            this.startIndex = startIndex;
            this.endIndex = endIndex;
        }

        Token(TokenType type, Integer startIndex, Integer endIndex) {
            this(type, null, startIndex, endIndex);
        }

        public String toString() {
            return (s == null) ? type.toString() : (type + "(" + s + ")");
        }

        public void unparse(StringBuilder buf) {
            if (s == null) {
                if (originTokenType != null) {
                    buf.append(originTokenType);
                } else {
                    buf.append(type.sql());
                }
            } else {
                buf.append(s);
            }
        }
    }

    /**
     * Token representing an identifier.
     */
    public static class IdToken extends Token {
        public IdToken(TokenType type, String s) {
            super(type, s);
            assert (type == TokenType.DQID) || (type == TokenType.ID);
        }

        public IdToken(TokenType type, String s, Integer startIndex, Integer endIndex) {
            super(type, s, startIndex, endIndex);
            assert (type == TokenType.DQID) || (type == TokenType.ID);
        }
    }

    /**
     * Token representing a query.
     */
    @SuppressWarnings("MissingSwitchDefault")
    static class Query extends Token {
        private final List<Token> tokenList;

        Query(List<Token> tokenList) {
            super(TokenType.QUERY);
            this.tokenList = new ArrayList<>(tokenList);
        }

        public void unparse(StringBuilder buf) {
            int k = -1;
            for (Token token : tokenList) {
                if (++k > 0) {
                    buf.append(' ');
                }
                token.unparse(buf);
            }
        }

        public static void simplifyList(List<Token> list, String hintToken) {
            // Simplify
            //   SELECT * FROM t UNION ALL SELECT * FROM u WHERE ^
            // to
            //   SELECT * FROM u WHERE ^
            for (Token token : list) {
                if (token instanceof Query) {
                    Query query = (Query) token;
                    if (query.contains(hintToken)) {
                        list.clear();
                        list.add(query.simplify(hintToken));
                        break;
                    }
                }
            }
        }

        public Query simplify(String hintToken) {
            TokenType clause = TokenType.SELECT;
            TokenType foundInClause = null;
            Query foundInSubQuery = null;
            TokenType majorClause = null;
            if (hintToken != null) {
                for (Token token : tokenList) {
                    switch (token.type) {
                        case ID:
                            if (hintToken.equals(token.s)) {
                                foundInClause = clause;
                            }
                            break;
                        case SELECT:
                        case FROM:
                        case WHERE:
                        case GROUP:
                        case HAVING:
                        case ORDER:
                            majorClause = token.type;
                            // fall through
                        case JOIN:
                        case USING:
                        case ON:
                            clause = token.type;
                            break;
                        case COMMA:
                            if (majorClause == TokenType.FROM) {
                                // comma inside from clause
                                clause = TokenType.FROM;
                            }
                            break;
                        case QUERY:
                            if (((Query) token).contains(hintToken)) {
                                foundInClause = clause;
                                foundInSubQuery = (Query) token;
                            }
                            break;
                    }
                }
            } else {
                foundInClause = TokenType.QUERY;
            }
            if (foundInClause != null) {
                switch (foundInClause) {
                    case SELECT:
                        purgeSelectListExcept(hintToken);
                        purgeWhere();
                        purgeOrderBy();
                        break;
                    case FROM:
                    case JOIN:

                        // See comments against ON/USING.
                        purgeSelect();
                        purgeFromExcept(hintToken);
                        purgeWhere();
                        purgeGroupByHaving();
                        purgeOrderBy();
                        break;
                    case ON:
                    case USING:

                        // We need to treat expressions in FROM and JOIN
                        // differently than ON and USING. Consider
                        //     FROM t1 JOIN t2 ON b1 JOIN t3 USING (c2)
                        // t1, t2, t3 occur in the FROM clause, and do not depend
                        // on anything; b1 and c2 occur in ON scope, and depend
                        // on the FROM clause
                        purgeSelect();
                        purgeWhere();
                        purgeOrderBy();
                        break;
                    case WHERE:
                        purgeSelect();
                        purgeGroupByHaving();
                        purgeOrderBy();
                        break;
                    case GROUP:
                    case HAVING:
                        purgeSelect();
                        purgeWhere();
                        purgeOrderBy();
                        break;
                    case ORDER:
                        purgeWhere();
                        break;
                    case QUERY:

                        // Indicates that the expression to be simplified is
                        // outside this sub-query. Preserve a simplified SELECT
                        // clause.
                        // It might be a good idea to purge select expressions, however
                        // purgeSelectExprsKeepAliases might end up with <<0 as "*">> which is not valid.
                        // purgeSelectExprsKeepAliases();
                        purgeWhere();
                        purgeGroupByHaving();
                        break;
                }
            }

            // Simplify sub-queries.
            for (Token token : tokenList) {
                switch (token.type) {
                    case QUERY: {
                        Query query = (Query) token;
                        query.simplify(
                                (query == foundInSubQuery) ? hintToken : null);
                        break;
                    }
                }
            }
            return this;
        }

        private void purgeSelectListExcept(String hintToken) {
            List<Token> sublist = findClause(TokenType.SELECT);
            int parenCount = 0;
            int itemStart = 1;
            int itemEnd = -1;
            boolean found = false;
            for (int i = 0; i < sublist.size(); i++) {
                Token token = sublist.get(i);
                switch (token.type) {
                    case LPAREN:
                        ++parenCount;
                        break;
                    case RPAREN:
                        --parenCount;
                        break;
                    case COMMA:
                        if (parenCount == 0) {
                            if (found) {
                                itemEnd = i;
                                break;
                            }
                            itemStart = i + 1;
                        }
                        break;
                    case ID:
                        if (hintToken.equals(token.s)) {
                            found = true;
                        }
                }
            }
            if (found) {
                if (itemEnd < 0) {
                    itemEnd = sublist.size();
                }

                List<Token> selectItem =
                        new ArrayList<>(
                                sublist.subList(itemStart, itemEnd));
                Token select = sublist.get(0);
                sublist.clear();
                sublist.add(select);
                sublist.addAll(selectItem);
            }
        }

        private void purgeSelect() {
            List<Token> sublist = findClause(TokenType.SELECT);
            Token select = sublist.get(0);
            sublist.clear();
            sublist.add(select);
            sublist.add(new Token(TokenType.ID, "*"));
        }

        private void purgeSelectExprsKeepAliases() {
            List<Token> sublist = findClause(TokenType.SELECT);
            List<Token> newSelectClause = new ArrayList<>();
            newSelectClause.add(sublist.get(0));
            int itemStart = 1;
            for (int i = 1; i < sublist.size(); i++) {
                Token token = sublist.get(i);
                if (((i + 1) == sublist.size())
                        || (sublist.get(i + 1).type == TokenType.COMMA)) {
                    if (token.type == TokenType.ID) {
                        // This might produce <<0 as "a.x+b.y">>, or <<0 as "*">>, or even <<0 as "a.*">>
                        newSelectClause.add(new Token(TokenType.ID, "0"));
                        newSelectClause.add(new Token(TokenType.ID, "AS"));
                        newSelectClause.add(token);
                    } else {
                        newSelectClause.addAll(
                                sublist.subList(itemStart, i + 1));
                    }
                    itemStart = i + 2;
                    if ((i + 1) < sublist.size()) {
                        newSelectClause.add(new Token(TokenType.COMMA));
                    }
                }
            }
            sublist.clear();
            sublist.addAll(newSelectClause);
        }

        private void purgeFromExcept(String hintToken) {
            List<Token> sublist = findClause(TokenType.FROM);
            int itemStart = -1;
            int itemEnd = -1;
            int joinCount = 0;
            boolean found = false;
            for (int i = 0; i < sublist.size(); i++) {
                Token token = sublist.get(i);
                switch (token.type) {
                    case QUERY:
                        if (((Query) token).contains(hintToken)) {
                            found = true;
                        }
                        break;
                    case JOIN:
                        ++joinCount;
                        // fall through
                    case FROM:
                    case ON:
                    case COMMA:
                        if (found) {
                            itemEnd = i;
                            break;
                        }
                        itemStart = i + 1;
                        break;
                    case ID:
                        if (hintToken.equals(token.s)) {
                            found = true;
                        }
                }
            }

            // Don't simplify a FROM clause containing a JOIN: we lose help
            // with syntax.
            if (found && (joinCount == 0)) {
                if (itemEnd == -1) {
                    itemEnd = sublist.size();
                }
                List<Token> fromItem =
                        new ArrayList<>(
                                sublist.subList(itemStart, itemEnd));
                Token from = sublist.get(0);
                sublist.clear();
                sublist.add(from);
                sublist.addAll(fromItem);
            }
            if (sublist.get(sublist.size() - 1).type == TokenType.ON) {
                sublist.add(new Token(TokenType.ID, "TRUE"));
            }
        }

        private void purgeWhere() {
            List<Token> sublist = findClause(TokenType.WHERE);
            if (sublist != null) {
                sublist.clear();
            }
        }

        private void purgeGroupByHaving() {
            List<Token> sublist = findClause(TokenType.GROUP);
            if (sublist != null) {
                sublist.clear();
            }
            sublist = findClause(TokenType.HAVING);
            if (sublist != null) {
                sublist.clear();
            }
        }

        private void purgeOrderBy() {
            List<Token> sublist = findClause(TokenType.ORDER);
            if (sublist != null) {
                sublist.clear();
            }
        }

        private List<Token> findClause(TokenType keyword) {
            int start = -1;
            int k = -1;
            EnumSet<TokenType> clauses =
                    EnumSet.of(
                            TokenType.SELECT,
                            TokenType.FROM,
                            TokenType.WHERE,
                            TokenType.GROUP,
                            TokenType.HAVING,
                            TokenType.ORDER);
            for (Token token : tokenList) {
                ++k;
                if (token.type == keyword) {
                    start = k;
                } else if ((start >= 0)
                        && clauses.contains(token.type)) {
                    return tokenList.subList(start, k);
                }
            }
            if (start >= 0) {
                return tokenList.subList(start, k + 1);
            }
            return null;
        }


        private boolean contains(String hintToken) {
            for (Token token : tokenList) {
                switch (token.type) {
                    case ID:
                        if (hintToken.equals(token.s)) {
                            return true;
                        }
                        break;
                    case QUERY:
                        if (((Query) token).contains(hintToken)) {
                            return true;
                        }
                        break;
                }
            }
            return false;
        }
    }
}
