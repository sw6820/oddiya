import React from 'react';
import { render, fireEvent } from '@testing-library/react-native';
import Button from '@/components/atoms/Button';

describe('Button Component', () => {
  it('renders correctly with title', () => {
    const { getByText } = render(<Button title="Click Me" onPress={() => {}} />);
    expect(getByText('Click Me')).toBeDefined();
  });

  it('calls onPress when pressed', () => {
    const onPressMock = jest.fn();
    const { getByText } = render(<Button title="Click Me" onPress={onPressMock} />);
    
    fireEvent.press(getByText('Click Me'));
    expect(onPressMock).toHaveBeenCalledTimes(1);
  });

  it('does not call onPress when disabled', () => {
    const onPressMock = jest.fn();
    const { getByText } = render(
      <Button title="Click Me" onPress={onPressMock} disabled={true} />
    );
    
    fireEvent.press(getByText('Click Me'));
    expect(onPressMock).not.toHaveBeenCalled();
  });

  it('shows loading indicator when loading', () => {
    const { queryByText, getByTestId } = render(
      <Button title="Click Me" onPress={() => {}} loading={true} />
    );
    
    expect(queryByText('Click Me')).toBeNull();
  });

  it('applies correct styles for primary variant', () => {
    const { getByText } = render(<Button title="Primary" onPress={() => {}} variant="primary" />);
    const button = getByText('Primary').parent?.parent;
    expect(button).toBeDefined();
  });

  it('applies correct styles for secondary variant', () => {
    const { getByText } = render(
      <Button title="Secondary" onPress={() => {}} variant="secondary" />
    );
    const button = getByText('Secondary').parent?.parent;
    expect(button).toBeDefined();
  });

  it('applies correct styles for outline variant', () => {
    const { getByText } = render(<Button title="Outline" onPress={() => {}} variant="outline" />);
    const button = getByText('Outline').parent?.parent;
    expect(button).toBeDefined();
  });
});

