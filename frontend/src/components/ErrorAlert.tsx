import React from 'react';
import { AlertCircle } from 'lucide-react';

interface ErrorAlertProps {
  title?: string;
  message: string;
  onRetry?: () => void;
  className?: string;
}

export const ErrorAlert: React.FC<ErrorAlertProps> = ({
  title = 'Something went wrong',
  message,
  onRetry,
  className = '',
}) => {
  return (
    <div className={`p-6 bg-rose-50 border border-rose-200 rounded-2xl flex items-start space-x-4 ${className}`}>
      <AlertCircle className="w-6 h-6 text-rose-600 flex-shrink-0 mt-0.5" />
      <div className="flex-1">
        <h4 className="text-base font-semibold text-rose-900">{title}</h4>
        <p className="text-sm text-rose-700 mt-1">{message}</p>
        {onRetry && (
          <button
            onClick={onRetry}
            className="mt-3 text-sm font-semibold text-rose-700 hover:text-rose-900 underline"
          >
            Try again
          </button>
        )}
      </div>
    </div>
  );
};
