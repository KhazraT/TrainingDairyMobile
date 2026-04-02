package ru.squidory.trainingdairymobile;

import org.junit.Test;
import static org.junit.Assert.*;

import ru.squidory.trainingdairymobile.data.model.ProgramRequest;
import ru.squidory.trainingdairymobile.data.model.ProgramResponse;
import ru.squidory.trainingdairymobile.data.repository.ProgramRepository;

/**
 * Unit tests for Program Repository.
 */
public class ProgramRepositoryTest {

    @Test
    public void programRepository_getInstance_returnsSameInstance() {
        ProgramRepository instance1 = ProgramRepository.getInstance();
        ProgramRepository instance2 = ProgramRepository.getInstance();

        assertNotNull(instance1);
        assertSame(instance1, instance2);
    }

    @Test
    public void programRequest_model_worksCorrectly() {
        ProgramRequest request = new ProgramRequest();
        request.setName("Набор массы");
        request.setDescription("Программа для набора мышечной массы");
        request.setIsPublic(false);

        assertEquals("Набор массы", request.getName());
        assertEquals("Программа для набора мышечной массы", request.getDescription());
        assertEquals(Boolean.FALSE, request.getIsPublic());
    }

    @Test
    public void programResponse_model_worksCorrectly() {
        ProgramResponse response = new ProgramResponse();
        response.setId(1L);
        response.setUserId(21L);
        response.setName("Набор массы");
        response.setDescription("Программа для набора массы");
        response.setIsPublic(false);

        assertEquals(1L, response.getId());
        assertEquals(21L, response.getUserId());
        assertEquals("Набор массы", response.getName());
        assertEquals("Программа для набора массы", response.getDescription());
        assertEquals(Boolean.FALSE, response.getIsPublic());
    }

    @Test
    public void addition_isCorrect() {
        assertEquals(4, 2 + 2);
    }
}
