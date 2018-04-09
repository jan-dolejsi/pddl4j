/*
 * Copyright (c) 2016 by Damien Pellier <Damien.Pellier@imag.fr>.
 *
 * This file is part of PDDL4J library.
 *
 * PDDL4J is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 *
 * PDDL4J is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty
 * of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with PDDL4J.  If not, see
 * <http://www.gnu.org/licenses/>
 */

package fr.uga.pddl4j.planners;

import fr.uga.pddl4j.exceptions.FileException;
import fr.uga.pddl4j.heuristics.relaxation.Heuristic;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.util.Properties;

/**
 * This abstract class defines the main methods of to access a planner.
 *
 * @author D. Pellier
 * @version 1.0 - 12.04.2016
 *
 * @since 3.0
 */
public abstract class AbstractPlanner implements Planner {

    /**
     * The enumeration of the arguments of the planner.
     */
    public enum Argument {
        /**
         * The planning domain.
         */
        DOMAIN,
        /**
         * The planning problem.
         */
        PROBLEM,
        /**
         * The heuristic to use.
         */
        HEURISTIC,
        /**
         * The weight of the heuristic.
         */
        WEIGHT,
        /**
         * The global time slot allocated to the search.
         */
        TIMEOUT,
        /**
         * The trace level.
         */
        TRACE_LEVEL,
        /**
         * Generate statistics or not.
         */
        STATISTICS
    }

    /**
     * The timeout for the search in second.
     */
    private int timeout;

    /**
     * The trace level.
     */
    private int traceLevel;

    /**
     * The statistics of the planner.
     */
    private Statistics statistics;

    /**
     * Creates a new planner.
     */
    public AbstractPlanner() {
        super();
        this.timeout = Planner.DEFAULT_TIMEOUT;
        this.traceLevel = Planner.DEFAULT_TRACE_LEVEL;
        this.statistics = new Statistics();
    }

    /**
     * Returns the statistics of the planner.
     *
     * @return the statistics of the planner or null if no problem was solved.
     * @see Statistics
     */
    @Override
    public final Statistics getStatistics() {
        return this.statistics;
    }

    /**
     * Sets the time out of the planner.
     *
     * @param timeout the time allocated to the search in second. Timeout mus be positive.
     */
    @Override
    public final void setTimeOut(final int timeout) {
        this.timeout = timeout;
    }

    /**
     * Returns the time out of the planner.
     *
     * @return the time out of the planner, i.e., the time allocated to the search in second.
     */
    @Override
    public int getTimeout() {
        return this.timeout;
    }

    /**
     * Sets the trace level of the planner.
     *
     * @param level the trace level of the planner. Trace level must be positive.
     */
    @Override
    public final void setTraceLevel(final int level) {
        this.traceLevel = level;
    }

    /**
     * Returns the trace level of the planner.
     *
     * @return the trace level of the planner.
     */
    @Override
    public final int getTraceLevel() {
        return this.traceLevel;
    }

    /**
     * This method print the usage of the command-line planner.
     */
    private static StringBuilder printUsage() {

        final StringBuilder strb = new StringBuilder();

        strb.append("\nusage of planner:\n")
            .append("OPTIONS   DESCRIPTIONS\n")
            .append("-o <str>    operator file name\n")
            .append("-f <str>    fact file name\n")
            .append("-w <num>    the weight used in the a star seach (preset: 1)\n")
            .append("-t <num>    specifies the maximum CPU-time in seconds (preset: 300)\n")
            .append("-u <num>    specifies the heuristic to used (preset: 0)\n")
            .append("     0      ff heuristic\n")
            .append("     1      sum heuristic\n")
            .append("     2      sum mutex heuristic\n")
            .append("     3      adjusted sum heuristic\n")
            .append("     4      adjusted sum 2 heuristic\n")
            .append("     5      adjusted sum 2M heuristic\n")
            .append("     6      combo heuristic\n")
            .append("     7      max heuristic\n")
            .append("     8      set-level heuristic\n")
            .append("-i <num>    run-time information level (preset: 1)\n")
            .append("     0      nothing\n")
            .append("     1      info on action number, search and search\n")
            .append("     2      1 + info on problem constants, types and predicates\n")
            .append("     3      1 + 2 + loaded operators, initial and goal state\n")
            .append("     4      1 + predicates and their inertia status\n")
            .append("     5      1 + 4 + goal state and operators with unary inertia encoded\n")
            .append("     6      1 + actions, initial and goal state after expansion of variables\n")
            .append("     7      1 + final domain representation\n")
            .append("     8      line representation:\n")
            .append("               - problem name\n")
            .append("               - number of operators\n")
            .append("               - number of facts\n")
            .append("               - parsing time in seconds\n")
            .append("               - encoding time in seconds\n")
            .append("               - searching time in seconds\n")
            .append("               - total time in seconds\n")
            .append("               - memory used for problem representation in MBytes\n")
            .append("               - memory used for searching in MBytes\n")
            .append("               - total memory used in MBytes\n")
            .append("               - length of the solution plan\n")
            .append("-s <bool>   generate statistics or not (preset: true)\n")
            .append("-h          print this message\n\n");

        return strb;
    }

    /**
     * This method parse the command line and return the arguments.
     *
     * @param args the arguments from the command line.
     * @return The arguments of the planner.
     */
    public static Properties parseArguments(String[] args, Logger log, Properties defaultArguments)
        throws FileException {

        final Properties arguments = defaultArguments;
        try {
            for (int i = 0; i < args.length; i += 2) {
                if ("-o".equalsIgnoreCase(args[i]) && ((i + 1) < args.length)) {
                    if (!new File(args[i + 1]).exists()) {
                        log.trace("operators file does not exist: " + args[i + 1] + "\n");
                    }
                    arguments.put(AbstractPlanner.Argument.DOMAIN, new File(args[i + 1]));
                } else if ("-f".equalsIgnoreCase(args[i]) && ((i + 1) < args.length)) {
                    if (!new File(args[i + 1]).exists()) {
                        log.trace("facts file does not exist: " + args[i + 1] + "\n");
                    }
                    arguments.put(AbstractPlanner.Argument.PROBLEM, new File(args[i + 1]));
                } else if ("-t".equalsIgnoreCase(args[i]) && ((i + 1) < args.length)) {
                    final int cpu = Integer.parseInt(args[i + 1]) * 1000;
                    if (cpu < 0) {
                        log.trace(AbstractPlanner.printUsage());
                    }
                    arguments.put(AbstractPlanner.Argument.TIMEOUT, cpu);
                } else if ("-u".equalsIgnoreCase(args[i]) && ((i + 1) < args.length)) {
                    final int heuristic = Integer.parseInt(args[i + 1]);
                    if (heuristic < 0 || heuristic > 8) {
                        log.trace(AbstractPlanner.printUsage());
                    }
                    if (heuristic == 0) {
                        arguments.put(AbstractPlanner.Argument.HEURISTIC, Heuristic.Type.FAST_FORWARD);
                    } else if (heuristic == 1) {
                        arguments.put(AbstractPlanner.Argument.HEURISTIC, Heuristic.Type.SUM);
                    } else if (heuristic == 2) {
                        arguments.put(AbstractPlanner.Argument.HEURISTIC, Heuristic.Type.SUM_MUTEX);
                    } else if (heuristic == 3) {
                        arguments.put(AbstractPlanner.Argument.HEURISTIC, Heuristic.Type.AJUSTED_SUM);
                    } else if (heuristic == 4) {
                        arguments.put(AbstractPlanner.Argument.HEURISTIC, Heuristic.Type.AJUSTED_SUM2);
                    } else if (heuristic == 5) {
                        arguments.put(AbstractPlanner.Argument.HEURISTIC, Heuristic.Type.AJUSTED_SUM2M);
                    } else if (heuristic == 6) {
                        arguments.put(AbstractPlanner.Argument.HEURISTIC, Heuristic.Type.COMBO);
                    } else if (heuristic == 7) {
                        arguments.put(AbstractPlanner.Argument.HEURISTIC, Heuristic.Type.MAX);
                    } else {
                        arguments.put(AbstractPlanner.Argument.HEURISTIC, Heuristic.Type.SET_LEVEL);
                    }
                } else if ("-w".equalsIgnoreCase(args[i]) && ((i + 1) < args.length)) {
                    final double weight = Double.parseDouble(args[i + 1]);
                    if (weight < 0) {
                        log.trace(AbstractPlanner.printUsage());
                    }
                    arguments.put(AbstractPlanner.Argument.WEIGHT, weight);
                } else if ("-i".equalsIgnoreCase(args[i]) && ((i + 1) < args.length)) {
                    final int level = Integer.parseInt(args[i + 1]);
                    if (level < 0) {
                        log.trace(AbstractPlanner.printUsage());
                    }
                    arguments.put(AbstractPlanner.Argument.TRACE_LEVEL, level);
                } else if ("-s".equalsIgnoreCase(args[i]) && ((i + 1) < args.length)) {
                    final boolean isStatUsed = Boolean.parseBoolean(args[i + 1]);
                    arguments.put(AbstractPlanner.Argument.STATISTICS, isStatUsed);
                } else {
                    log.trace("\nUnknown argument for \"" + args[i] + "\" or missing value\n");
                    log.trace(AbstractPlanner.printUsage());
                    throw new FileException("Unknown arguments: " + args[i]);
                }
            }
            if (arguments.get(AbstractPlanner.Argument.DOMAIN) == null
                || arguments.get(AbstractPlanner.Argument.PROBLEM) == null) {

                log.trace("\nMissing DOMAIN or PROBLEM\n");
                log.trace(AbstractPlanner.printUsage());
                throw new FileException("Missing domain or problem");
            }
        } catch (RuntimeException runExp) {
            log.trace("\nError when parsing arguments\n");
            log.trace(AbstractPlanner.printUsage());
            throw runExp;
        }
        return arguments;
    }

}
