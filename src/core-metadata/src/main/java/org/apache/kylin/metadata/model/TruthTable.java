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

package org.apache.kylin.metadata.model;

import java.util.ArrayDeque;
import java.util.Collections;
import java.util.Comparator;
import java.util.Deque;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;
import org.apache.kylin.guava30.shaded.common.base.Preconditions;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class TruthTable<E> {

    private final List<Expr<E>> allOperands;
    private final Expr<E> expr;

    TruthTable(List<Expr<E>> allOperands, Expr<E> expr) {
        this.allOperands = allOperands;
        this.expr = expr;
    }

    @Override
    public String toString() {
        return "TruthTable{" + "allOperands=" + allOperands + ", expr=" + expr + '}';
    }

    @Override
    public int hashCode() {
        return allOperands.hashCode() * 31 + expr.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof TruthTable)) {
            return false;
        }
        TruthTable<E> tbl2 = (TruthTable<E>) obj;
        log.debug("Comparing table1: {} with table2 {}", this, tbl2);
        if (this.allOperands.size() != tbl2.allOperands.size()) {
            return false;
        }
        for (Expr<E> operand : this.allOperands) {
            if (!tbl2.allOperands.contains(operand)) {
                return false;
            }
        }

        InputGenerator<E> tbl1InputGenerator = new InputGenerator<>();
        while (tbl1InputGenerator.hasNext()) {
            Input<E> input = tbl1InputGenerator.next();
            if (this.eval(input) != tbl2.eval(input.mapOperands(tbl2.allOperands))) {
                return false;
            }
        }
        return true;
    }

    public boolean eval(Input<E> input) {
        return doEval(expr, input);
    }

    private boolean doEval(Expr<E> expr, Input<E> input) {
        switch (expr.op) {
        case IDENTITY:
            return input.getValue(expr);
        case NOT:
            return !doEval(expr.operands.get(0), input);
        case AND:
            for (Expr<E> innerExpr : expr.operands) {
                if (!doEval(innerExpr, input)) {
                    return false;
                }
            }
            return true;
        case OR:
            for (Expr<E> innerExpr : expr.operands) {
                if (doEval(innerExpr, input)) {
                    return true;
                }
            }
            return false;
        default:
            throw new IllegalStateException("Invalid Operator" + expr.op);
        }
    }

    private static class Input<T> {
        private final Map<Expr<T>, Boolean> inputValues;

        Input(Map<Expr<T>, Boolean> inputValues) {
            this.inputValues = inputValues;
        }

        boolean getValue(Expr<T> expr) {
            return inputValues.get(expr);
        }

        /**
         * map input with operands in different orderings
         */
        Input<T> mapOperands(List<Expr<T>> operands) {
            Preconditions.checkArgument(operands.size() == inputValues.size());
            Map<Expr<T>, Boolean> mappedInput = new HashMap<>();
            for (Expr<T> expr : operands) {
                Preconditions.checkArgument(inputValues.get(expr) != null, "Invalid table expr for operands mapping");
                mappedInput.put(expr, inputValues.get(expr));
            }
            Preconditions.checkArgument(mappedInput.size() == inputValues.size());
            return new Input<>(mappedInput);
        }
    }

    private class InputGenerator<T> implements Iterator<Input<T>> {

        int inputSize = allOperands.size();
        int inputMax = (int) Math.pow(2, inputSize);
        int inputBits = 0;

        @Override
        public boolean hasNext() {
            return inputBits < inputMax;
        }

        @Override
        public Input<T> next() {
            if (!hasNext()) {
                throw new NoSuchElementException();
            }
            Map<Expr<T>, Boolean> input = new HashMap<>();
            if (log.isTraceEnabled()) {
                log.trace("Generating next input {}, max inputValues bits {}", Integer.toBinaryString(inputBits),
                        Integer.toBinaryString(inputMax - 1));
            }
            for (int i = 0; i < inputSize; i++) {
                boolean value = (inputBits & (1 << i)) != 0;
                input.put((Expr<T>) allOperands.get(i), value);
            }
            inputBits++;
            return new Input<>(input);
        }
    }

    public enum Operator {
        AND, OR, NOT, IDENTITY
    }

    static class Expr<T> {
        Operator op;
        List<Expr<T>> operands;
        T operand;
        Comparator<T> operandComparator;

        Expr(Operator op, Comparator<T> operandComparator, List<Expr<T>> operands) {
            this.op = op;
            this.operands = operands;
            this.operandComparator = operandComparator;
        }

        Expr(T operand, Comparator<T> operandComparator) {
            this.op = Operator.IDENTITY;
            this.operand = operand;
            this.operandComparator = operandComparator;
            this.operands = Collections.emptyList();
        }

        @Override
        public boolean equals(Object obj) {
            if (!(obj instanceof Expr)) {
                return false;
            }

            Expr<T> that = (Expr<T>) obj;
            if (!(op == that.op && operands.size() == that.operands.size()
                    && operandComparator.compare(operand, that.operand) == 0)) {
                return false;
            }
            for (int i = 0; i < operands.size(); i++) {
                if (!Objects.equals(operands.get(i), that.operands.get(i))) {
                    return false;
                }
            }
            return true;
        }

        @Override
        public int hashCode() {
            final int prime = 31;
            int result = 1;
            result = prime * result + op.hashCode();
            result = prime * result + operand.hashCode();
            for (Expr<T> expr : operands) {
                result = prime * result + expr.hashCode();
            }
            return result;
        }

        @Override
        public String toString() {
            String exprStr = StringUtils.join(operands, ", ", "[", "]");
            return "Expr{op=" + op + ", operands=" + exprStr + ", operand=" + operand + '}';
        }
    }

    public static class ExprBuilder<T> {
        Set<Expr<T>> allOperandSet = new HashSet<>();
        Deque<Operator> operatorStack = new ArrayDeque<>();
        Deque<List<Expr<T>>> exprsStack = new ArrayDeque<>();
        Comparator<T> operandComparator;

        public ExprBuilder(Comparator<T> operandComparator) {
            this.operandComparator = operandComparator;
            exprsStack.push(new LinkedList<>());
        }

        public ExprBuilder<T> compositeStart(Operator operator) {
            operatorStack.push(operator);
            exprsStack.push(new LinkedList<>());
            return this;
        }

        public ExprBuilder<T> compositeEnd() {
            Expr<T> composited = new Expr<>(operatorStack.pop(), operandComparator, exprsStack.pop());
            Objects.requireNonNull(exprsStack.peek()).add(composited);
            return this;
        }

        public ExprBuilder<T> addExpr(T operand) {
            Expr<T> expr = new Expr<>(operand, operandComparator);
            allOperandSet.add(expr);
            Objects.requireNonNull(exprsStack.peek()).add(expr);
            return this;
        }

        protected Expr<T> build() {
            return Objects.requireNonNull(exprsStack.peek()).get(0);
        }
    }
}
