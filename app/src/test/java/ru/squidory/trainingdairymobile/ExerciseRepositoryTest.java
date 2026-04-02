package ru.squidory.trainingdairymobile;

import org.junit.Test;
import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.List;

import ru.squidory.trainingdairymobile.data.model.ExerciseResponse;
import ru.squidory.trainingdairymobile.data.model.MuscleGroupResponse;
import ru.squidory.trainingdairymobile.data.repository.ExerciseRepository;

/**
 * Unit tests for Exercise Repository.
 */
public class ExerciseRepositoryTest {

    @Test
    public void exerciseRepository_getInstance_returnsSameInstance() {
        // Given
        ExerciseRepository instance1 = ExerciseRepository.getInstance();
        ExerciseRepository instance2 = ExerciseRepository.getInstance();

        // Then
        assertNotNull(instance1);
        assertSame(instance1, instance2);
    }

    @Test
    public void exerciseResponse_model_worksCorrectly() {
        // Given
        ExerciseResponse exercise = new ExerciseResponse();
        exercise.setId(1L);
        exercise.setName("Жим лёжа");
        exercise.setDescription("Базовое упражнение для груди");
        exercise.setExerciseType("reps_weight");

        // When
        long id = exercise.getId();
        String name = exercise.getName();
        String description = exercise.getDescription();
        String type = exercise.getExerciseType();

        // Then
        assertEquals(1L, id);
        assertEquals("Жим лёжа", name);
        assertEquals("Базовое упражнение для груди", description);
        assertEquals("reps_weight", type);
    }

    @Test
    public void exerciseResponse_withMuscleGroups_worksCorrectly() {
        // Given
        ExerciseResponse exercise = new ExerciseResponse();
        List<MuscleGroupResponse> muscles = new ArrayList<>();
        
        MuscleGroupResponse chest = new MuscleGroupResponse();
        chest.setId(1L);
        chest.setName("Грудь");
        muscles.add(chest);
        
        MuscleGroupResponse triceps = new MuscleGroupResponse();
        triceps.setId(6L);
        triceps.setName("Трицепсы");
        muscles.add(triceps);
        
        exercise.setMuscleGroups(muscles);

        // Then
        assertEquals(2, exercise.getMuscleGroups().size());
        assertEquals("Грудь", exercise.getMuscleGroups().get(0).getName());
        assertEquals("Трицепсы", exercise.getMuscleGroups().get(1).getName());
    }

    @Test
    public void addition_isCorrect() {
        assertEquals(4, 2 + 2);
    }
}
