import React, { useState } from 'react';
import {
  View,
  Text,
  StyleSheet,
  ScrollView,
  TextInput,
  TouchableOpacity,
  ActivityIndicator,
  Platform,
} from 'react-native';
import { generatePlanStreaming, StreamMessages } from '@/api/streaming';
import { CreatePlanRequest, TravelPlan } from '@/types';
import { useAppDispatch } from '@/store';
import { fetchPlans, createPlan } from '@/store/slices/plansSlice';

interface Props {
  navigation: any;
}

const CreatePlanScreen: React.FC<Props> = ({ navigation }) => {
  const dispatch = useAppDispatch();

  // Form state
  const [location, setLocation] = useState('Seoul');
  const [startDate, setStartDate] = useState('2025-11-10');
  const [endDate, setEndDate] = useState('2025-11-12');
  const [budget, setBudget] = useState<'low' | 'medium' | 'high'>('medium');

  // Streaming state
  const [isGenerating, setIsGenerating] = useState(false);
  const [progress, setProgress] = useState(0);
  const [statusMessage, setStatusMessage] = useState('');
  const [chunks, setChunks] = useState<string[]>([]);
  const [isCached, setIsCached] = useState(false);
  const [generatedPlan, setGeneratedPlan] = useState<TravelPlan | null>(null);
  const [startTime, setStartTime] = useState(0);
  const [elapsedTime, setElapsedTime] = useState(0);

  const handleGenerate = async () => {
    if (!location || !startDate || !endDate) {
      alert('Please fill in all fields');
      return;
    }

    setIsGenerating(true);
    setProgress(0);
    setStatusMessage('초기화 중...');
    setChunks([]);
    setIsCached(false);
    setGeneratedPlan(null);
    const startTimestamp = Date.now();
    setStartTime(startTimestamp);

    // Start timer
    const timer = setInterval(() => {
      setElapsedTime((Date.now() - startTimestamp) / 1000);
    }, 100);

    const request: CreatePlanRequest = {
      location,
      startDate,
      endDate,
      budget,
    };

    try {
      const plan = await generatePlanStreaming(request, {
        onStatus: (message, prog) => {
          setStatusMessage(message);
          setProgress(prog);
        },
        onProgress: (message, prog) => {
          setStatusMessage(message);
          setProgress(prog);
        },
        onChunk: content => {
          setChunks(prev => [...prev, content]);
        },
        onComplete: async (plan, cached) => {
          setGeneratedPlan(plan);
          setIsCached(cached);
          setProgress(100);
          setStatusMessage(cached ? StreamMessages.CACHED_COMPLETE : StreamMessages.COMPLETE);
          setElapsedTime((Date.now() - startTimestamp) / 1000);
          clearInterval(timer);

          // Save the generated plan to Plan Service
          try {
            // Convert budget level to estimated amount
            const budgetMap = {
              low: 50000,
              medium: 100000,
              high: 200000,
            };

            // Calculate total budget based on days
            const days = plan.days?.length || 3;
            const dailyBudget = budgetMap[request.budget || 'medium'];
            const totalBudget = dailyBudget * days;

            const savePlanRequest = {
              title: plan.title,
              destination: request.location,
              startDate: request.startDate,
              endDate: request.endDate,
              budget: totalBudget,
            };

            await dispatch(createPlan(savePlanRequest as any)).unwrap();
            // Refresh plans list to show the newly saved plan
            await dispatch(fetchPlans()).unwrap();
          } catch (saveError) {
            console.error('Failed to save plan:', saveError);
            // Still show the plan even if save fails
            alert('Plan generated but not saved. Please try again.');
          }
        },
        onError: error => {
          alert('Error: ' + error);
          setIsGenerating(false);
          clearInterval(timer);
        },
      });

      console.log('Plan generated:', plan);
    } catch (error) {
      console.error('Generation error:', error);
      setIsGenerating(false);
      clearInterval(timer);
    } finally {
      setIsGenerating(false);
      clearInterval(timer);
    }
  };

  const handleViewPlan = () => {
    if (generatedPlan) {
      navigation.navigate('PlanDetail', { planId: generatedPlan.id });
    }
  };

  const budgetOptions = [
    { value: 'low', label: 'Low', description: '₩50,000/day' },
    { value: 'medium', label: 'Medium', description: '₩100,000/day' },
    { value: 'high', label: 'High', description: '₩200,000+/day' },
  ];

  return (
    <ScrollView style={styles.container} contentContainerStyle={styles.content}>
      <View style={styles.header}>
        <Text style={styles.title}>🚀 AI Travel Planner</Text>
        <Text style={styles.subtitle}>Create your personalized travel plan</Text>
      </View>

      {/* Form */}
      <View style={styles.form}>
        <View style={styles.formGroup}>
          <Text style={styles.label}>📍 Destination</Text>
          <TextInput
            style={styles.input}
            value={location}
            onChangeText={setLocation}
            placeholder="e.g., Seoul, Busan, Jeju"
            editable={!isGenerating}
          />
        </View>

        <View style={styles.row}>
          <View style={[styles.formGroup, styles.halfWidth]}>
            <Text style={styles.label}>📅 Start Date</Text>
            <TextInput
              style={styles.input}
              value={startDate}
              onChangeText={setStartDate}
              placeholder="YYYY-MM-DD"
              editable={!isGenerating}
            />
          </View>

          <View style={[styles.formGroup, styles.halfWidth]}>
            <Text style={styles.label}>📅 End Date</Text>
            <TextInput
              style={styles.input}
              value={endDate}
              onChangeText={setEndDate}
              placeholder="YYYY-MM-DD"
              editable={!isGenerating}
            />
          </View>
        </View>

        <View style={styles.formGroup}>
          <Text style={styles.label}>💰 Budget Level</Text>
          <View style={styles.budgetContainer}>
            {budgetOptions.map(option => (
              <TouchableOpacity
                key={option.value}
                style={[
                  styles.budgetOption,
                  budget === option.value && styles.budgetOptionActive,
                ]}
                onPress={() => !isGenerating && setBudget(option.value as any)}
                disabled={isGenerating}>
                <Text
                  style={[
                    styles.budgetLabel,
                    budget === option.value && styles.budgetLabelActive,
                  ]}>
                  {option.label}
                </Text>
                <Text
                  style={[
                    styles.budgetDesc,
                    budget === option.value && styles.budgetDescActive,
                  ]}>
                  {option.description}
                </Text>
              </TouchableOpacity>
            ))}
          </View>
        </View>

        <TouchableOpacity
          style={[styles.generateButton, isGenerating && styles.generateButtonDisabled]}
          onPress={handleGenerate}
          disabled={isGenerating}>
          {isGenerating ? (
            <ActivityIndicator color="#FFFFFF" />
          ) : (
            <Text style={styles.generateButtonText}>Generate Travel Plan ✨</Text>
          )}
        </TouchableOpacity>
      </View>

      {/* Progress Section */}
      {isGenerating && (
        <View style={styles.progressSection}>
          <View style={styles.progressBarContainer}>
            <View style={[styles.progressBar, { width: `${progress}%` }]} />
          </View>

          <View style={[styles.statusBox, isCached && styles.statusBoxCached]}>
            <Text style={styles.statusIcon}>{isCached ? '💾' : '⏳'}</Text>
            <Text style={styles.statusText}>{statusMessage}</Text>
            <Text style={styles.progressText}>{progress}%</Text>
          </View>

          {chunks.length > 0 && (
            <View style={styles.chunksContainer}>
              <Text style={styles.chunksTitle}>AI Output:</Text>
              <ScrollView style={styles.chunksScroll}>
                {chunks.map((chunk, index) => (
                  <Text key={index} style={styles.chunk}>
                    {chunk}
                  </Text>
                ))}
              </ScrollView>
            </View>
          )}

          <Text style={styles.timer}>Time: {elapsedTime.toFixed(1)}s</Text>
        </View>
      )}

      {/* Generated Plan Preview */}
      {generatedPlan && (
        <View style={styles.planPreview}>
          <View style={styles.planHeader}>
            <View>
              <Text style={styles.planTitle}>{generatedPlan.title}</Text>
              {isCached && <Text style={styles.cachedBadge}>💾 Cached</Text>}
            </View>
            <Text style={styles.planCost}>
              ₩{(generatedPlan.totalEstimatedCost || 0).toLocaleString()}
            </Text>
          </View>

          <Text style={styles.planSummary}>
            {generatedPlan.days?.length || 0} days • Generated in {elapsedTime.toFixed(1)}s
          </Text>

          <TouchableOpacity style={styles.viewButton} onPress={handleViewPlan}>
            <Text style={styles.viewButtonText}>View Full Plan →</Text>
          </TouchableOpacity>
        </View>
      )}
    </ScrollView>
  );
};

const styles = StyleSheet.create({
  container: {
    flex: 1,
    backgroundColor: '#F5F5F5',
  },
  content: {
    padding: 20,
  },
  header: {
    marginBottom: 24,
  },
  title: {
    fontSize: 28,
    fontWeight: '700',
    color: '#333',
    marginBottom: 8,
  },
  subtitle: {
    fontSize: 14,
    color: '#666',
  },
  form: {
    backgroundColor: '#FFFFFF',
    borderRadius: 16,
    padding: 20,
    marginBottom: 20,
    ...Platform.select({
      ios: {
        shadowColor: '#000',
        shadowOffset: { width: 0, height: 2 },
        shadowOpacity: 0.1,
        shadowRadius: 8,
      },
      android: {
        elevation: 4,
      },
    }),
  },
  formGroup: {
    marginBottom: 16,
  },
  label: {
    fontSize: 14,
    fontWeight: '600',
    color: '#333',
    marginBottom: 8,
  },
  input: {
    borderWidth: 2,
    borderColor: '#E0E0E0',
    borderRadius: 8,
    padding: 12,
    fontSize: 14,
    color: '#333',
  },
  row: {
    flexDirection: 'row',
    gap: 12,
  },
  halfWidth: {
    flex: 1,
  },
  budgetContainer: {
    gap: 8,
  },
  budgetOption: {
    borderWidth: 2,
    borderColor: '#E0E0E0',
    borderRadius: 8,
    padding: 12,
    backgroundColor: '#FFFFFF',
  },
  budgetOptionActive: {
    borderColor: '#667eea',
    backgroundColor: '#F0F4FF',
  },
  budgetLabel: {
    fontSize: 14,
    fontWeight: '600',
    color: '#333',
  },
  budgetLabelActive: {
    color: '#667eea',
  },
  budgetDesc: {
    fontSize: 12,
    color: '#666',
    marginTop: 2,
  },
  budgetDescActive: {
    color: '#667eea',
  },
  generateButton: {
    backgroundColor: '#667eea',
    borderRadius: 12,
    padding: 16,
    alignItems: 'center',
    marginTop: 8,
  },
  generateButtonDisabled: {
    backgroundColor: '#ccc',
  },
  generateButtonText: {
    color: '#FFFFFF',
    fontSize: 16,
    fontWeight: '600',
  },
  progressSection: {
    backgroundColor: '#FFFFFF',
    borderRadius: 16,
    padding: 20,
    marginBottom: 20,
  },
  progressBarContainer: {
    height: 8,
    backgroundColor: '#E0E0E0',
    borderRadius: 10,
    overflow: 'hidden',
    marginBottom: 16,
  },
  progressBar: {
    height: '100%',
    backgroundColor: '#667eea',
  },
  statusBox: {
    flexDirection: 'row',
    alignItems: 'center',
    backgroundColor: '#F5F5F5',
    borderRadius: 8,
    padding: 12,
    marginBottom: 12,
  },
  statusBoxCached: {
    backgroundColor: '#E8F5E9',
  },
  statusIcon: {
    fontSize: 20,
    marginRight: 8,
  },
  statusText: {
    flex: 1,
    fontSize: 14,
    color: '#333',
  },
  progressText: {
    fontSize: 14,
    fontWeight: '600',
    color: '#667eea',
  },
  chunksContainer: {
    backgroundColor: '#FAFAFA',
    borderRadius: 8,
    padding: 12,
    marginBottom: 12,
  },
  chunksTitle: {
    fontSize: 12,
    fontWeight: '600',
    color: '#666',
    marginBottom: 8,
  },
  chunksScroll: {
    maxHeight: 150,
  },
  chunk: {
    fontSize: 12,
    color: '#333',
    marginBottom: 4,
    fontFamily: Platform.OS === 'ios' ? 'Menlo' : 'monospace',
  },
  timer: {
    fontSize: 12,
    color: '#666',
    textAlign: 'center',
  },
  planPreview: {
    backgroundColor: '#F0F4FF',
    borderRadius: 16,
    padding: 20,
    borderWidth: 2,
    borderColor: '#667eea',
  },
  planHeader: {
    flexDirection: 'row',
    justifyContent: 'space-between',
    alignItems: 'flex-start',
    marginBottom: 8,
  },
  planTitle: {
    fontSize: 20,
    fontWeight: '700',
    color: '#333',
    marginBottom: 4,
  },
  cachedBadge: {
    fontSize: 12,
    color: '#2196F3',
    fontWeight: '600',
  },
  planCost: {
    fontSize: 18,
    fontWeight: '700',
    color: '#764ba2',
  },
  planSummary: {
    fontSize: 14,
    color: '#666',
    marginBottom: 16,
  },
  viewButton: {
    backgroundColor: '#667eea',
    borderRadius: 8,
    padding: 14,
    alignItems: 'center',
  },
  viewButtonText: {
    color: '#FFFFFF',
    fontSize: 14,
    fontWeight: '600',
  },
});

export default CreatePlanScreen;
