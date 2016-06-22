package org.camunda.tngp.broker.wf.runtime;

import org.camunda.tngp.graph.bpmn.ExecutionEventType;
import org.camunda.tngp.taskqueue.data.BpmnProcessEventEncoder;
import org.camunda.tngp.taskqueue.data.MessageHeaderEncoder;
import org.camunda.tngp.util.buffer.BufferWriter;

import uk.co.real_logic.agrona.MutableDirectBuffer;

public class BpmnProcessEventWriter implements BufferWriter
{

    protected MessageHeaderEncoder headerEncoder;
    protected BpmnProcessEventEncoder bodyEncoder;

    protected long key;
    protected long processId;
    protected long processInstanceId;
    protected ExecutionEventType event;
    protected int initialElementId;

    @Override
    public int getLength()
    {
        return MessageHeaderEncoder.ENCODED_LENGTH
                + BpmnProcessEventEncoder.BLOCK_LENGTH;
    }

    @Override
    public void write(MutableDirectBuffer buffer, int offset)
    {
        headerEncoder.wrap(buffer, offset);
        headerEncoder
            .blockLength(bodyEncoder.sbeBlockLength())
            .templateId(bodyEncoder.sbeTemplateId())
            .schemaId(bodyEncoder.sbeSchemaId())
            .version(bodyEncoder.sbeSchemaVersion())
            // TODO: make resourceId and shardId setters
            .resourceId(0)
            .shardId(0);

        bodyEncoder.wrap(buffer, offset + headerEncoder.encodedLength())
            .key(key)
            .processId(processId)
            .processInstanceId(processInstanceId)
            .event(event.value());

    }

    public BpmnProcessEventWriter key(long key)
    {
        this.key = key;
        return this;
    }

    public BpmnProcessEventWriter processId(long processId)
    {
        this.processId = processId;
        return this;
    }

    public BpmnProcessEventWriter processInstanceId(long processInstanceId)
    {
        this.processInstanceId = processInstanceId;
        return this;
    }

    public BpmnProcessEventWriter event(ExecutionEventType event)
    {
        this.event = event;
        return this;
    }

    public BpmnProcessEventWriter initialElementId(int initialElementId)
    {
        this.initialElementId = initialElementId;
        return this;
    }

}
