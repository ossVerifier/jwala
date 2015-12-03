package com.siemens.cto.aem.commandprocessor.impl;

import com.siemens.cto.aem.commandprocessor.CommandExecutor;
import com.siemens.cto.aem.commandprocessor.CommandProcessor;
import com.siemens.cto.aem.commandprocessor.CommandProcessorBuilder;
import com.siemens.cto.aem.exec.CommandOutput;
import com.siemens.cto.aem.exec.ExecReturnCode;
import com.siemens.cto.aem.exception.CommandFailureException;
import com.siemens.cto.aem.exception.NotYetReturnedException;
import com.siemens.cto.aem.io.FullInputStreamReaderTask;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;

public class ThreadedCommandExecutorImpl implements CommandExecutor {

    private static final Logger LOGGER = LoggerFactory.getLogger(ThreadedCommandExecutorImpl.class);
    
    private final ExecutorService executorService;

    public ThreadedCommandExecutorImpl(final ExecutorService theExecutorService) {
        executorService = theExecutorService;
    }

    @Override
    public CommandOutput execute(final CommandProcessorBuilder aBuilder) throws CommandFailureException {

        try (final CommandProcessor processor = aBuilder.build()) {

            final Future<String> standardOutput = consumeOutput(processor.getCommandOutput());
            final Future<String> standardError = consumeOutput(processor.getErrorOutput());
            final ExecReturnCode returnCode = getReturnCodeWhenFinished(processor,
                                                                        standardOutput,
                                                                        standardError);
            
            LOGGER.debug("after consuming standard output and standard err");
            return new CommandOutput(returnCode,
                                get(standardOutput),
                                get(standardError));
        } catch (final NotYetReturnedException nyre) {
            throw new CommandFailureException(nyre.getCommand(),
                                              nyre);
        } catch (IOException ioe) {
            throw new RuntimeException("Unexpected IOException from java.io.Closeable.close();",
                                       ioe);
        }
    }

    protected Future<String> consumeOutput(final InputStream someInput) {
        return executorService.submit(new FullInputStreamReaderTask(someInput));
    }

    protected ExecReturnCode getReturnCodeWhenFinished(final CommandProcessor aProcessor,
                                                       final Future<?>... someFutures) throws NotYetReturnedException {

        for (final Future<?> future : someFutures) {
            get(future);
        }

        return aProcessor.getExecutionReturnCode();
    }

    protected <T> T get(final Future<T> aFuture) {

        try {
            return aFuture.get();
        } catch (final InterruptedException | ExecutionException e) {
            throw new RuntimeException(e); // by agreement
        }
    }
}
