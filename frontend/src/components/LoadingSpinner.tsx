import React from 'react';
import { Loader2 } from 'lucide-react';

interface LoadingSpinnerProps {
  message?: string;
  className?: string;
}

export const LoadingSpinner: React.FC<LoadingSpinnerProps> = ({
  message = 'Loading data...',
  className = '',
}) => {
  return (
    <div className={`flex flex-col items-center justify-center py-16 px-4 ${className}`}>
      <Loader2 className="w-10 h-10 text-brand-600 animate-spin mb-3" />
      <p className="text-sm font-medium text-slate-500">{message}</p>
    </div>
  );
};
