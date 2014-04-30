package com.siemens.cto.aem.commandprocessor.impl;

import java.io.IOException;
import java.io.InputStream;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;

import com.siemens.cto.aem.commandprocessor.CommandExecutor;
import com.siemens.cto.aem.commandprocessor.CommandProcessor;
import com.siemens.cto.aem.commandprocessor.CommandProcessorBuilder;
import com.siemens.cto.aem.commandprocessor.domain.ExecutionData;
import com.siemens.cto.aem.commandprocessor.domain.ExecutionReturnCode;
import com.siemens.cto.aem.commandprocessor.domain.NotYetReturnedException;
import com.siemens.cto.aem.exception.CommandFailureException;
import com.siemens.cto.aem.io.FullInputStreamReaderTask;

public class ThreadedCommandExecutorImpl implements CommandExecutor {

    private final ExecutorService executorService;

    public ThreadedCommandExecutorImpl(final ExecutorService theExecutorService) {
        executorService = theExecutorService;
    }

    @Override
    public ExecutionData execute(final CommandProcessorBuilder aBuilder) throws CommandFailureException {

        try (final CommandProcessor processor = aBuilder.build()) {

            final Future<String> standardOutput = consumeOutput(processor.getCommandOutput());
            final Future<String> standardError = consumeOutput(processor.getErrorOutput());
            final ExecutionReturnCode returnCode = getReturnCodeWhenFinished(processor,
                                                                             standardOutput,
                                                                             standardError);

            return new ExecutionData(returnCode,
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

    protected ExecutionReturnCode getReturnCodeWhenFinished(final CommandProcessor aProcessor,
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
            //TODO Decide how to handle these exceptions in a real-world scenario
            throw new RuntimeException(e);
        }
    }
}
